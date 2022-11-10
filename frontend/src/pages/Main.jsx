import { motion } from "framer-motion";
import { container } from "../utils/util";
import React, { useEffect, useState } from "react";
import {
  Routes,
  Route,
  useLocation,
  Link,
  useNavigate,
} from "react-router-dom";
import styled, { keyframes, css } from "styled-components";
import { Box, Text } from "grommet";
import { MainMYContents } from "../components/main/MainMYContents";
import UpArrowIcon from "../assets/icons/upArrowIcon.svg";
import BackArrowIcon from "../assets/icons/backArrowIcon.svg";
import { BottomSheet } from "react-spring-bottom-sheet";
import "react-spring-bottom-sheet/dist/style.css";
import PlomonSample1 from "../assets/PlomonSample1.gif";
import PlomonSample2 from "../assets/PlomonSample2.gif";
import PlomonSample3 from "../assets/PlomonSample3.gif";
import PlomonSample4 from "../assets/PlomonSample4.gif";
import Switch from '@mui/material/Switch';



const MainCategoryContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1vh 0 1vh 0;
  width: 100%;
  height: 5vh;
  z-index: 1;
`;

const MainMyCategory = styled.div`
  font-size: 18px;
  font-weight: bold;
  color: #57ba83;
  padding: 0 2vh 0 2vh;
  cursor: pointer;
  &:after {
    content: "";
    display: block;
    width: 18px;
    border-bottom: 2px solid #57ba83;
    margin: auto;
  }
`;

const MainExploreCategory = styled.div`
  font-size: 18px;
  font-weight: bold;
  color: rgba(87, 186, 131, 0.6);
  padding: 0 2vh 0 2vh;
  cursor: pointer;
  &:after {
    content: "";
    display: block;
    width: 0px;
    border-bottom: 2px solid #57ba83;
    margin: auto;
  }
`;

const PopUpButton = styled.div`
  // display: flex;
  // justify-content: center;
  // align-items: center;
  // flex-direction: column;
  height: 80px;
  width: 100vw;
  z-index: 1;
  margin-bottom:45px;
  position: fixed;
  bottom: 0;
`;

const PopUpButtonArea = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
  height: 2vh;
  z-index: 1;
  animation: motion 0.5s linear 0s infinite alternate;
  @keyframes motion {
    0% {
      margin-top: 0px;
    }
    100% {
      margin-top: 10px;
    }
  }
`;

const PlomonTableTitle = styled.div`
  padding-top: 3%;
  padding-left: 5%;
  height: 30px;
  font-size: 19px;
  font-weight: bold;
  color: #232323;
  display: flex;
  align-items: center;
  
`

const PlomonTableArea = styled.div`
  width: 96%;
  display: flex;
  flex-wrap: wrap;
  padding-top: 4%;
  padding-left: 4%;
`

const SmallPlomon = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 4vw;
  padding-right: 4vw;
  padding-bottom: 4vw;
`

const PlomonName = styled.div`
  font-size: 16px;
  font-weight: bold;
  color: #535353;
  padding-top: 2vw;
`

const PlomonState = styled.div`
  background-color: #57BA83;
  color: white;
  font-size: 10px;
  font-weight: 500;
  margin-top: 2vw;
  padding: 0 8px 1px 8px;
  border-radius: 15px;
`

const PlomonDetailName = styled.div`
  font-size: 18px;
  font-weight: bold;
  color: #535353;
  padding-top: 5vw;
`

const PlomonDetailState = styled.div`
  background-color: #57BA83;
  color: white;
  font-size: 14px;
  font-weight: 500;
  margin: 1vh 0 2vh 0;
  padding: 0 12px 2px 12px;
  border-radius: 20px;
`

const PlomonDetailText = styled.div`
  font-size: 16px;
  font-weight: bold;
  color: #535353;
  margin-right: 10px;

`

const ProgressBar = styled.progress`
  appearance: none;
  height: 4px;
  width:56vw;
  margin-top:1px;
  ::-webkit-progress-bar {
    border-radius: 5px;
    background: #c1c1c1;
  }

  ::-webkit-progress-value {
    border-radius: 5px;
    background: #57ba83;
  }
`;

const label = { inputProps: { 'aria-label': 'Switch demo' } };



export const Main = () => {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [plomonOpen, setPlomonOpen] = useState(false);
  const [isPlomonClicked, setIsPlomonClicked] = useState(false);
  useEffect(()=>{
},[plomonOpen, isPlomonClicked]);
  return (
    <div
      style={{
        width: "100%",
        justify: "center",
        height: "100vh",
        background:
          "linear-gradient(307.96deg, rgba(87, 186, 131, 0.296), rgba(29, 38, 255, 0.088))",
        textAlign: "center",
        display: "flex",
        justifyContent: "space-between",
        position: "relative",
      }}
    >
      <Box
        height="100%"
        width="100%"
        justify="between"
        style={{ position: "absolute" }}
      >
        <MainCategoryContainer>
          <MainMyCategory onClick={() => navigate("/main")}>MY</MainMyCategory>
          <MainExploreCategory onClick={() => navigate("/mainexplore")}>
            탐험하기
          </MainExploreCategory>
        </MainCategoryContainer>
        <PopUpButton onClick={() => setOpen(true)}>
          <div style={{ fontSize: "17px", fontWeight: "500", color: "#B2B2B2" }}>
            모아보기
          </div>
          <PopUpButtonArea>
            <img width="25px" height="25px" src={UpArrowIcon} />
          </PopUpButtonArea>
        </PopUpButton>
      </Box>

      {/* 모아보기 */}
      <BottomSheet
        open={open}
        onDismiss={() => setOpen(false)}
        snapPoints={({ maxHeight }) => 0.93 * maxHeight}
      >
      <PlomonTableTitle onClick={() => setOpen(false)}>
        <img style={{width:"30px", height:"30px", paddingRight:"20px"}} src={BackArrowIcon} />
        모아보기
      </PlomonTableTitle>
      <PlomonTableArea>
        <SmallPlomon onClick={() => (setPlomonOpen(true), setOpen(false), setIsPlomonClicked(false)) }>
          <img style={{width:"28vw", height:"24vw", objectFit:'cover'}} src={PlomonSample1}/>
          <PlomonName>재권</PlomonName>
          <PlomonState>Baby</PlomonState>
        </SmallPlomon>
        <SmallPlomon>
          <img style={{width:"28vw", height:"24vw", objectFit:'cover'}} src={PlomonSample2}/>
          <PlomonName>피스</PlomonName>
          <PlomonState>Baby</PlomonState>
        </SmallPlomon>
        <SmallPlomon>
          <img style={{width:"28vw", height:"24vw", objectFit:'cover'}} src={PlomonSample3}/>
          <PlomonName>키치</PlomonName>
          <PlomonState>Baby</PlomonState>
        </SmallPlomon>
        <SmallPlomon>
          <img style={{width:"28vw", height:"24vw", objectFit:'cover'}} src={PlomonSample4}/>
          <PlomonName>레이</PlomonName>
          <PlomonState>Baby</PlomonState>
        </SmallPlomon>
      </PlomonTableArea>
      </BottomSheet>


      {/* 플로몬 디테일 */}
      <BottomSheet
        open={plomonOpen}
        onDismiss={() => setPlomonOpen(false)}
        snapPoints={({ maxHeight }) => 0.93 * maxHeight}
      >
      <PlomonTableTitle onClick={() => (isPlomonClicked===false ? (setPlomonOpen(false), setOpen(true)): setPlomonOpen(false))}>
        <img style={{width:"30px", height:"30px", paddingRight:"20px"}} src={BackArrowIcon} />
        재권
      </PlomonTableTitle>
      <PlomonTableArea>
        <SmallPlomon>
          <img style={{width:"92vw", height:"50vw", objectFit:'cover'}} src={PlomonSample1}/>
          <PlomonDetailName>재권</PlomonDetailName>
          <PlomonDetailState>Baby</PlomonDetailState>
          <Box margin="2vh 0" direction="row" justify="between" align="center" width="90%">
            <PlomonDetailText>
              경험치
            </PlomonDetailText>
            <ProgressBar
              id="progress"
              value={67}
              min="0"
              max="100"
            ></ProgressBar>
            <Text size="12px" weight={400}>
              {67}%
            </Text>
          </Box>
          <Box margin="2vh 0" direction="row" justify="between" align="center" width="90%">
            <PlomonDetailText>
              변신
            </PlomonDetailText>
            <div style={{margin:"1px 52vw 0 0"}}>
              <Switch {...label}/>
            </div>
          </Box>
        </SmallPlomon>
      </PlomonTableArea>
      </BottomSheet>

      <MainMYContents style={{ position: "absolute" }} setPlomonOpen={setPlomonOpen} setIsPlomonClicked={setIsPlomonClicked}/>
    </div>
  );
};
