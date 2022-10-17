import React, { useState } from 'react';
import styled from 'styled-components';
import IntroductionText from './IntroductionText';
import DatePicker from './DatePicker';
import PeopleCounterInput from './PeopleCounterInput';
import RegionCategoryBox from './RegionCategoryBox';
import TitleInput from './TitleInput';
import ThemeCategoryBox from './ThemeCategoryBox';

const ContentsEditorBox = styled.div`
  background: salmon;
  width: 50%;
  height: 100%;
  padding: 16px;
`;

function ContentsEditor() {
  const initialState = {
    memberEmail: '1',
    title: 'testTitle',
    startDay: '2022-10-16',
    endDay: '2022-10-17',
    peopleCount: 0,
    disclosure: 'public',
    placeThumbnailUrl: 'testPlaceThumbnailUrl',
    introductionText: 'testIntroductionText',
    journeyThemes: ['힐링', '식도락'],
  };

  const [journeyInfo, setJourneyInfo] = useState(initialState);
  console.log(journeyInfo.datas);

  const updateData = (newState) => {
    setJourneyInfo((prev) => ({
      ...prev,
      newState,
    }));
  };

  return (
    <ContentsEditorBox>
      <TitleInput datas={journeyInfo} onUpdate={updateData} />
      <RegionCategoryBox />
      <DatePicker />
      <DatePicker />
      <PeopleCounterInput datas={journeyInfo} onUpdate={updateData} />
      <IntroductionText datas={journeyInfo} onUpdate={updateData} />
      <ThemeCategoryBox />
    </ContentsEditorBox>
  );
}

export default ContentsEditor;
