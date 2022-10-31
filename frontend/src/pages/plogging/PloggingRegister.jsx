import React, { useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { container } from "../../utils/util";
import { Box } from "grommet";
import BackBtn from "../../assets/images/backButton.png";
import PlusBtn from "../../assets/images/plus.png";
import PloggingTitle from "../../assets/images/ploggingTitle.png";
import PloggingTitleBlack from "../../assets/images/ploggingTitleBlack.png";
import { StyledText } from "../../components/Common";
import { ContentSelectBottomSheet } from "../../components/plogging/ContentSelectBottomSheet";
import {
  BootstrapButton,
  ContentChooseButton,
} from "../../components/common/Buttons";
import domtoimage from "dom-to-image";
import { saveAs } from "file-saver";
import { Map, Polyline } from "react-kakao-maps-sdk";
export const PloggingRegister = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [contentColor, setContentColor] = useState("white");
  const { ploggingType, ploggingData, lineImage, lineImageBlack, address } =
    location.state;
  // ploggingType: "",
  //     ploggingData: {
  //       latlng: mapData.latlng,
  //       kcal: data.kcal,
  //       time: time,
  //       totalDistance: data.totalDistance,
  //     },
  const [data, setData] = useState(ploggingData);
  const [open, setOpen] = useState(false);
  const [format, setFormat] = useState([]);
  const [image, setImage] = useState(null);
  const [prev, setPrev] = useState(null);
  const imageRef = useRef();

  // 사진 추가
  const handleImageUpload = (e) => {
    const fileArr = e.target.files;
    console.log(fileArr);
    let fileURL = "";
    if (fileArr.length !== 0) {
      // console.log(fileArr[0]);
      setImage(fileArr[0]);
      let reader = new FileReader();
      reader.onload = () => {
        fileURL = reader.result;
        setPrev(fileURL);
        // console.log(fileURL);
      };
      reader.readAsDataURL(fileArr[0]);
    }
  };

  return (
    <motion.div
      initial="hidden"
      animate="visible"
      variants={container}
      style={{
        position: "relative",
        textAlign: "center",
        height: "100vh",
      }}
    >
      {/* 헤더 */}
      <Box
        width="100%"
        height="7%"
        direction="row"
        justify="around"
        align="center"
      >
        {/* 뒤로가기 버튼 */}
        <Box width="10%">
          <img
            src={BackBtn}
            onClick={() => {
              navigate(-1);
            }}
            style={{
              width: "14px",
              fill: "cover",
            }}
          />
        </Box>
        {/* 포스터 꾸미기 */}
        <Box width="60%" align="center">
          <StyledText text="포스터 꾸미기" weight="bold" size="18px" />
        </Box>
        {/* 빈칸 */}
        <Box width="10%"></Box>
      </Box>
      {/* 사진 박스 */}
      <motion.div
        variants={container}
        animate={{ height: "auto" }}
        style={{
          width: "100%",
          // backgroundImage:
          //   prev === null
          //     ? "url('/assets/images/defaultPic.png')"
          //     : `url('${prev}')`,
          // backgroundRepeat: "no-repeat",
          position: "relative",
          zIndex: "1500",
        }}
        ref={imageRef}
        onClick={() => {
          if (contentColor === "white") setContentColor("black");
          else setContentColor("white");
        }}
      >
        <Box
          width="100%"
          height="100%"
          align="center"
          justify="between"
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            zIndex: "150",
          }}
        >
          {format.find((element) => element === 1) !== undefined && (
            <img
              src={contentColor === "white" ? lineImage : lineImageBlack}
              style={{
                position: "absolute",
                width: "100%",
                height: "100%",
                zIndex: 150,
                fill: "cover",
              }}
            />
          )}
          {/* 헤더 */}
          <Box
            width="100%"
            direction="row"
            justify="between"
            pad={{
              left: "25px",
              top: "15px",
              right: "25px",
            }}
          >
            <img
              src={
                contentColor === "white" ? PloggingTitle : PloggingTitleBlack
              }
            />
            <Box justify="end" height="100%">
              {format.find((element) => element === 2) !== undefined && (
                <StyledText text={address} color={contentColor} weight="bold" />
              )}
            </Box>
          </Box>
          {/* 아래 내용 */}
          {format.find((element) => element === 0) !== undefined && (
            <Box
              direction="row"
              align="center"
              justify="around"
              gap="20vw"
              pad={{
                bottom: "25px",
              }}
            >
              <Box align="end" direction="row">
                <StyledText
                  text={data.totalDistance}
                  weight="bold"
                  size="20px"
                  color={contentColor}
                />
                <StyledText size="10px" text={"km"} color={contentColor} />
              </Box>
              <Box align="end" direction="row">
                <StyledText
                  text={data.time}
                  weight="bold"
                  size="20px"
                  color={contentColor}
                />
                <StyledText size="10px" text={"시간"} color={contentColor} />
              </Box>
              <Box align="end" direction="row">
                <StyledText
                  text={data.kcal}
                  weight="bold"
                  size="20px"
                  color={contentColor}
                />
                <StyledText size="10px" text={"kcal"} color={contentColor} />
              </Box>
            </Box>
          )}
        </Box>
        <img
          src={prev === null ? "/assets/images/defaultPic.png" : prev}
          style={{
            width: "100%",
            height: "100%",
            zIndex: 100,
            fill: "cover",
          }}
        />
      </motion.div>
      {/* 데이터 셀렉트 박스 */}
      <Box width="100%" height="50%" align="center">
        <label
          htmlFor="image"
          style={{
            display: "flex",
            width: "95%",
          }}
        >
          <motion.div
            whileTap={{ scale: 0.9 }}
            style={{
              textTransform: "none",
              fontSize: 16,
              fontWeight: "bold",
              padding: "6px 12px",
              color: "black",
              width: "90%",
              height: "43px",
              border: "none",
              margin: "10px",
              fontFamily: `shsnMedium, sans-serif`,
              backgroundColor: "white",
            }}
          >
            <Box direction="row" justify="between" width="100%">
              내 사진으로 추가하기
              <img src={PlusBtn} />
            </Box>
          </motion.div>
        </label>
        <input
          id="image"
          type="file"
          accept="image/jpg,image/png,image/jpeg,image/gif"
          style={{
            display: "none",
          }}
          onChange={handleImageUpload}
        />
        <ContentChooseButton
          onClick={() => {
            setOpen(true);
          }}
          whileTap={{ scale: 0.9 }}
        >
          <Box direction="row" justify="between">
            컨텐츠 추가하기
            <img src={PlusBtn} />
          </Box>
        </ContentChooseButton>
        <BootstrapButton
          style={{
            width: "75%",
            height: "50px",
          }}
          whileTap={{ scale: 0.9 }}
          onClick={() => {
            const ima = imageRef.current;
            domtoimage.toBlob(ima).then((blob) => {
              saveAs(blob, "card.png");
            });
          }}
        >
          {"확인"}
        </BootstrapButton>
      </Box>
      <ContentSelectBottomSheet
        open={open}
        onDismiss={() => {
          setOpen(false);
        }}
        format={format}
        setFormat={setFormat}
      />
    </motion.div>
  );
};
