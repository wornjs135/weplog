import { Box, Text } from "grommet";
import React, { useState } from "react";
import styled from "styled-components";
import crown from "../../assets/images/crown.svg";

const Profile1st = styled.img`
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid #ffd100;
`;

const Profile2nd = styled.img`
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid #c1c1c1;
`;

const Profile3rd = styled.img`
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid #ff9900;
`;

export const ChallengeRankTop3 = ({ type, top3, rankType }) => {
  console.log(top3);
  const changeImage = (id) => {
    if (type === 'user') {
      return top3[id].profileImageUrl;
    } else {
      return top3[id].imageUrl;
    }
  }
  const changeTitle = (id) => {
    if (type === 'user') {
      return top3[id].nickname;
    } else {
      return top3[id].name;
    }
  }
  const changevalue = (id) => {
    if (type === 'user') {
      switch (rankType) {
        case "DISTANCE":
          return top3[id].totalDistance + " Km";

        case "PLOGGING_CNT":
          return top3[id].totalCnt + " 회";

        case "TIME":
          return top3[id].totalTime;

        default:
          return undefined;
      }
    } else {
      return top3[id].totalAmount;
    }
  }
  if (top3 && top3.length > 0)
    return (
      <Box
        direction="row"
        justify="evenly"
        width="100%"
        align="end"
        margin="20px 0px"
      >
        <Box direction="column" height="85px" justify="between" align="center">
          <Profile2nd src={changeImage(1)} alt="프로필 사진" />
          <Box direction="column" align="center">
            <Text size="10px" weight={400}>
              {changeTitle(1)}
            </Text>
            <Text size="10px" weight={400}>
              {changevalue(1)}
            </Text>
          </Box>
        </Box>
        <Box
          direction="column"
          height="120px"
          justify="between"
          align="center"
          style={{ position: "relative" }}
        >
          <img
            src={crown}
            alt="왕관 사진"
            style={{ position: "absolute", top: "-20px", left: "30px" }}
          />
          <Profile1st src={changeImage(0)} alt="프로필 사진" />
          <Box direction="column" align="center">
            <Text size="12px" weight={500}>
              {changeTitle(0)}
            </Text>
            <Text size="10px" weight={400}>
              {changevalue(0)}
            </Text>
          </Box>
        </Box>
        <Box direction="column" height="85px" justify="between" align="center">
          <Profile3rd src={changeImage(2)} alt="프로필 사진" />
          <Box direction="column" align="center">
            <Text size="10px" weight={400}>
              {changeTitle(2)}
            </Text>
            <Text size="10px" weight={400}>
              {changevalue(2)}
            </Text>
          </Box>
        </Box>
      </Box>
    );
  else return <Box>없어요</Box>;
};