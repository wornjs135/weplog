package com.ssafy.challengeservice.service.challenge;


import com.ssafy.challengeservice.domain.challenge.Challenge;
import com.ssafy.challengeservice.domain.challengeranking.ChallengeRanking;
import com.ssafy.challengeservice.domain.challengetype.ChallengeType;
import com.ssafy.challengeservice.domain.member.Member;
import com.ssafy.challengeservice.domain.memberchallenge.MemberChallenge;
import com.ssafy.challengeservice.domain.redis.RedisChallengeRanking;
import com.ssafy.challengeservice.dto.*;
import com.ssafy.challengeservice.dto.response.CreateChallengeRes;
import com.ssafy.challengeservice.global.common.error.exception.DuplicateException;
import com.ssafy.challengeservice.global.common.error.exception.NotExistException;
import com.ssafy.challengeservice.global.common.error.exception.NotFoundException;
import com.ssafy.challengeservice.infra.s3.S3Upload;
import com.ssafy.challengeservice.messagequeue.KafkaProducer;
import com.ssafy.challengeservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ssafy.challengeservice.global.common.error.exception.DuplicateException.DUPLICATE_CHALLENGE_TYPE;
import static com.ssafy.challengeservice.global.common.error.exception.NotExistException.NO_EXIST_MEMBER_CHALLENGE;
import static com.ssafy.challengeservice.global.common.error.exception.NotFoundException.CHALLENGE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeServiceImpl implements ChallengeService{

    private final ChallengeRepository challengeRepository;
    private final ChallengeTypeRepository challengeTypeRepository;
    private final MemberChallengeRepository memberChallengeRepository;
    private final MemberRepository memberRepository;
    private final S3Upload s3Upload;
    private final ChallengeLimitRepository challengeLimitRepository;
    private RedisChallengeRankingRepository redisChallengeRankingRepository;
    private final ChallengeRankingRepository challengeRankingRepository;
    private final KafkaProducer kafkaProducer;

    // 챌린지 생성
    @Override
    @Transactional
    public CreateChallengeRes postChallenge(ChallengeReq challengeReq, MultipartFile image, String memberId) {
        ChallengeType challengeType = challengeTypeRepository.findChallengeTypeByTypeName(challengeReq.getType()).get();
        Member findMember = memberRepository.findById(UUID.fromString(memberId)).get();

        // 만드는 챌린지의 타입이 이미 참여한 타입의 챌린지 수를 초과했는지 체크
        // 참가하려는 타입의 중복 타입 챌린지(현재 진행중인) 참여 막기
        List<MemberChallenge> participationStatusAtType = memberChallengeRepository.findDuplicatedChallengeType(UUID.fromString(memberId), challengeType);
        if (participationStatusAtType.size() >= challengeType.getChallengeLimit().getMaxCnt()) {
            throw new DuplicateException(DUPLICATE_CHALLENGE_TYPE);
        }

        String imageUrl = s3Upload.uploadImageToS3(image);

        // 챌린지 저장
        Challenge saveChallenge = challengeRepository.save(challengeReq.toEntity(challengeType, findMember, imageUrl));

        // 챌린지에 참가
        MemberChallenge participateChallenge = MemberChallenge.builder()
                .member(findMember)
                .challenge(saveChallenge)
                .build();
        memberChallengeRepository.save(participateChallenge);
        saveChallenge.updateParticipantsCnt();

        return CreateChallengeRes.create(saveChallenge.getId());
    }

    @Override
    @Transactional
    public void deleteChallenge(Long challengeId, String memberId) {
        MemberChallenge memberChallenge = memberChallengeRepository.findByChallengeIdAndMemberId(challengeId, UUID.fromString(memberId))
                .orElseThrow(() -> new NotExistException(NO_EXIST_MEMBER_CHALLENGE));

        memberChallengeRepository.delete(memberChallenge);
        memberChallenge.getChallenge().exitChallenge();
        return;
    }

    @Override
    public Slice<ChallengeRes> getChallengeList(Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeList(pageable).map(m -> new ChallengeRes(m));
        return slice;
    }

    @Override
    public Slice<ChallengeRes> getMyChallengeList(String id, Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeByID(id, pageable).map(m->new ChallengeRes(m));
        return slice;
    }

    @Override
    public Slice<ChallengeRes> getChallengeBySearch(String title, Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeByTitle(title, pageable)
                .map(m ->new ChallengeRes(m));
        return slice;
    }

    @Override
    public ChallengeDetailRes getChallengeDetail(Long id) {
        Challenge challenge = challengeRepository.findByIdWithMember(id)
                .orElseThrow(() -> new NotFoundException(CHALLENGE_NOT_FOUND));

        return ChallengeDetailRes.from(challenge);
    }

    // end time 끝난 챌린지 종료시키기
    @Override
    @Transactional
    public void finishChallenge() {
        // 끝나야 할 챌린지 조회;
        List<Challenge> finishChallengeList = challengeRepository.findAllMustFinishChallenge();

        // 챌린지 finishFlag = true로 update
        challengeRepository.updateFinishChallenge();

        // 최종 랭킹 데이터 mysql에 넣기
        List<String> memberIdList = new ArrayList<>();
        List<ChallengeRanking> mysqlRankingData = new ArrayList<>(); // mysql에 들어갈 데이터 추가

        for (Challenge challenge : finishChallengeList) {
            memberIdList.add(challenge.getMember().getId().toString());  // 리워드 갱신되어야할 멤버들 (카프카로 전달할 내용)

            // 챌린지 별로 랭킹 조회해서 mysql에 저장
            List<ChallengeRankingDto> rankChallenge = this.getRankChallenge(challenge.getId());

            List<AddRewardPointDto> addRewardPointDtoList = new ArrayList<>();
            for (ChallengeRankingDto challengeRankingDto : rankChallenge) {
                Member findMember = memberRepository.findById(UUID.fromString(challengeRankingDto.getMemberId())).get();
                Challenge findChallenge = challengeRepository.findById(challengeRankingDto.getChallengeId()).get();
                mysqlRankingData.add(ChallengeRanking.create(challengeRankingDto, findMember, findChallenge));

                // 해당 챌린지가 달성됐으면 멤버에게 리워드 제공(카프카 전송)
                if (challenge.getProgress() >= challenge.getGoal()) {
                    addRewardPointDtoList.add(AddRewardPointDto.create(findMember.getId()
                            , challenge.getRewardPoint() * (1 + challengeRankingDto.getContribution() * 0.01)));
                }
            }

            // 챌린지 달성에 따른 리워드 포인트 제공
            kafkaProducer.sendRewardPoint("update-reward-point", addRewardPointDtoList);
        }
        challengeRankingRepository.saveAll(mysqlRankingData); // mysql에 저장

        // 챌린지 종료에 따른 도전과제 달성도 반영
        kafkaProducer.sendAchievement("exit-challenge", memberIdList);

    }

    // 챌린지 랭킹 조회
    @Override
    public List<ChallengeRankingDto> getRankChallenge(Long challengeId) {
        Challenge findChallenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException(CHALLENGE_NOT_FOUND));

        // 챌린지가 끝났으면 mysql에서 가져오기
        if (findChallenge.getFinishFlag()) {
            List<ChallengeRanking> challengeRankingByChallengeIdOrderByRankingWithMember
                    = challengeRankingRepository.getChallengeRankingByChallengeIdOrderByRankingWithMember(challengeId);

            return challengeRankingByChallengeIdOrderByRankingWithMember
                    .stream().map(ranking -> ChallengeRankingDto.from(ranking))
                    .collect(Collectors.toList());
        } else { // 챌린지가 안끝났으면 레디스나 데이터 갱신해서 가져오기
            List<ChallengeRankingDtoInterface> rankingByChallengeId = new ArrayList<>();
            if (findChallenge.getType().equals("DISTANCE")) {
                rankingByChallengeId =  challengeRepository.getDistanceRankingByChallengeId(challengeId);
            } else if (findChallenge.getType().equals("TIME")) {
                rankingByChallengeId =  challengeRepository.getTimeRankingByChallengeId(challengeId);
            } else if (findChallenge.getType().equals("PLOGGING_CNT")) {
                rankingByChallengeId =  challengeRepository.getCntRankingByChallengeId(challengeId);
            }

            return rankingByChallengeId.stream().map(dtoInterface -> ChallengeRankingDto.from(dtoInterface))
                    .collect(Collectors.toList());
        }
    }
}
