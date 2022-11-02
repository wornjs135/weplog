package com.ssafy.memberservice.domain.memberpet.domain;

import com.ssafy.memberservice.domain.member.domain.Member;
import com.ssafy.memberservice.domain.pet.domain.Pet;
import com.ssafy.memberservice.domain.pet.domain.PetCategory;
import com.ssafy.memberservice.global.common.base.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberPet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_pet_id")
    private Long id;

    private Integer currentLevel;
    private Integer currentExp;
    private Integer maxExp;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PetCategory name;

    private String fileUrl;
    private Integer imageLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Builder
    public MemberPet(Member member, Pet pet){
        this.member = member;
        this.pet = pet;
        this.currentExp = 0;
        this.currentLevel = 0;
        this.fileUrl = pet.getFileUrl();
        this.imageLevel = 0;
        this.maxExp = pet.getMaxExp();
        this.name = pet.getCategory();
    }

    @Builder
    public MemberPet(Member member, Pet pet, int currentLevel, int currentExp, int imageLevel){
        this.member = member;
        this.pet = pet;
        this.currentExp = currentLevel;
        this.currentLevel = currentExp;
        this.fileUrl = pet.getFileUrl();
        this.imageLevel = imageLevel;
        this.maxExp = pet.getMaxExp();
        this.name = pet.getCategory();
    }

}
