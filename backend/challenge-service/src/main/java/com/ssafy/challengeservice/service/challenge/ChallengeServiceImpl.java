package com.ssafy.challengeservice.service.challenge;


import com.ssafy.challengeservice.domain.challenge.Challenge;
import com.ssafy.challengeservice.domain.challengetype.ChallengeType;
import com.ssafy.challengeservice.domain.member.Member;
import com.ssafy.challengeservice.domain.memberchallenge.MemberChallenge;
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
    private final KafkaProducer kafkaProducer;

    // ????????? ??????
    @Override
    @Transactional
    public CreateChallengeRes postChallenge(ChallengeReq challengeReq, MultipartFile image, String memberId) {
        ChallengeType challengeType = challengeTypeRepository.findChallengeTypeByTypeName(challengeReq.getType()).get();
        Member findMember = memberRepository.findById(UUID.fromString(memberId)).get();

        // ????????? ???????????? ????????? ?????? ????????? ????????? ????????? ?????? ??????????????? ??????
        // ??????????????? ????????? ?????? ?????? ?????????(?????? ????????????) ?????? ??????
        List<MemberChallenge> participationStatusAtType = memberChallengeRepository.findDuplicatedChallengeType(UUID.fromString(memberId), challengeType);
        if (participationStatusAtType.size() >= challengeType.getChallengeLimit().getMaxCnt()) {
            throw new DuplicateException(DUPLICATE_CHALLENGE_TYPE);
        }

        String imageUrl = s3Upload.uploadImageToS3(image);

        // ????????? ??????
        Challenge saveChallenge = challengeRepository.save(challengeReq.toEntity(challengeType, findMember, imageUrl));

        // ???????????? ??????
        MemberChallenge participateChallenge = MemberChallenge.builder()
                .member(findMember)
                .challenge(saveChallenge)
                .totalAmount(0L)
                .nickname(findMember.getNickname())
                .profileImageUrl(findMember.getProfileImageUrl())
                .build();
        memberChallengeRepository.save(participateChallenge);
        saveChallenge.updateParticipantsCnt();

        return CreateChallengeRes.create(saveChallenge.getId());
    }

    @Override
    @Transactional
    public void deleteChallenge(Long challengeId, UUID memberId) {
        MemberChallenge memberChallenge = memberChallengeRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new NotExistException(NO_EXIST_MEMBER_CHALLENGE));

        memberChallengeRepository.delete(memberChallenge);
        memberChallenge.getChallenge().exitChallenge();
        return;
    }

    @Override
    public Slice<ChallengeRes> getChallengeList(Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeList(pageable).map(m -> ChallengeRes.create(m));
        return slice;
    }

    @Override
    public Slice<ChallengeRes> getMyChallengeList(UUID id, Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeByID(id, pageable).map(m->ChallengeRes.create(m));
        return slice;
    }

    @Override
    public Slice<ChallengeRes> getChallengeBySearch(String title, Pageable pageable) {
        Slice<ChallengeRes> slice = challengeRepository.getChallengeByTitle(title, pageable)
                .map(m ->ChallengeRes.create(m));
        return slice;
    }

    @Override
    public ChallengeDetailRes getChallengeDetail(Long id, UUID memberId) {
        Challenge challenge = challengeRepository.findByIdWithMember(id)
                .orElseThrow(() -> new NotFoundException(CHALLENGE_NOT_FOUND));

        return ChallengeDetailRes.from(challenge, memberId);
    }

    // end time ?????? ????????? ???????????????
    @Override
    @Transactional
    public void finishChallenge() {
        // ????????? ??? ????????? ??????;
        List<Challenge> finishChallengeList = challengeRepository.findAllMustFinishChallenge();

        // ????????? finishFlag = true??? update
        challengeRepository.updateFinishChallenge();

        // ????????? ?????? ??? ???????????? ????????? ?????????
        List<String> memberIdList = new ArrayList<>();

        for (Challenge challenge : finishChallengeList) {


            List<MemberChallenge> memberChallengeList = challenge.getMemberChallengeList();
            List<AddRewardPointDto> addRewardPointDtoList = new ArrayList<>();
            for (MemberChallenge memberChallenge : memberChallengeList) {
                memberIdList.add(memberChallenge.getMember().getId().toString());  // ????????? ?????????????????? ????????? (???????????? ????????? ??????)

                // ?????? ???????????? ??????????????? ???????????? ????????? ??????(????????? ??????)
                if (challenge.getProgress() >= challenge.getGoal()) {
                    addRewardPointDtoList.add(AddRewardPointDto.create(memberChallenge.getMember().getId()
                            , challenge.getRewardPoint() * (1 + (memberChallenge.getTotalAmount() / challenge.getGoal()))));
                }
            }

            // ????????? ????????? ?????? ????????? ????????? ??????
            if (!addRewardPointDtoList.isEmpty())
                kafkaProducer.sendRewardPoint("update-reward-point", addRewardPointDtoList);
        }

        // ????????? ????????? ?????? ???????????? ????????? ??????
        kafkaProducer.sendAchievement("exit-challenge", memberIdList);
    }

    // ????????? ?????? ??????
    @Override
    public List<ChallengeRankingDto> getRankChallenge(Long challengeId) {
        Challenge findChallenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException(CHALLENGE_NOT_FOUND));

            List<ChallengeRankingDtoInterface> challengeRankingByChallengeIdOrderByRankingWithMember
                    = challengeRepository.getRankingByChallengeId(challengeId);

            return challengeRankingByChallengeIdOrderByRankingWithMember
                    .stream().map(ranking -> ChallengeRankingDto.from(ranking))
                    .collect(Collectors.toList());
    }
}
