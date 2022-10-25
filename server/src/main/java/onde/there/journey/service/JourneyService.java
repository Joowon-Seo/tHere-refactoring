package onde.there.journey.service;

import static onde.there.domain.type.RegionType.findByRegion;
import static onde.there.journey.exception.JourneyErrorCode.DATE_ERROR;
import static onde.there.journey.exception.JourneyErrorCode.NOT_FOUND_JOURNEY;
import static onde.there.journey.exception.JourneyErrorCode.NOT_FOUND_MEMBER;
import static onde.there.journey.exception.JourneyErrorCode.YOU_ARE_NOT_THE_AUTHOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onde.there.domain.Journey;
import onde.there.domain.JourneyTheme;
import onde.there.domain.Member;
import onde.there.domain.type.JourneyThemeType;
import onde.there.dto.journy.JourneyDto;
import onde.there.dto.journy.JourneyDto.DetailResponse;
import onde.there.dto.journy.JourneyDto.JourneyListResponse;
import onde.there.dto.journy.JourneyDto.UpdateRequest;
import onde.there.dto.journy.JourneyDto.UpdateResponse;
import onde.there.dto.journy.JourneySearchTheme;
import onde.there.image.service.AwsS3Service;
import onde.there.journey.exception.JourneyException;
import onde.there.journey.repository.JourneyRepository;
import onde.there.journey.repository.JourneyRepositoryImpl;
import onde.there.journey.repository.JourneyThemeRepository;
import onde.there.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourneyService {

	private final JourneyRepository journeyRepository;
	private final JourneyThemeRepository journeyThemeRepository;
	private final MemberRepository memberRepository;
	private final JourneyRepositoryImpl journeyRepositoryImpl;
	private final AwsS3Service awsS3Service;

	@Transactional
	public JourneyDto.CreateResponse createJourney(
		JourneyDto.CreateRequest request, MultipartFile thumbnail) {

		log.info("createJourney() : 호출");

		Member member = memberRepository.findById(request.getMemberId())
			.orElseThrow(() -> new JourneyException(NOT_FOUND_MEMBER));

		if (request.getEndDate().isBefore(request.getStartDate())) {
			throw new JourneyException(DATE_ERROR);
		}

		List<String> imageUrls = awsS3Service.uploadFiles(
			Collections.singletonList(thumbnail));

		log.info("createJourney() : 여정 thumbnail 업로드 완료, "
			+ "(여정 thumbnail URL : " + imageUrls.get(0) + ")");

		Journey journey = Journey.builder()
			.member(member)
			.title(request.getTitle())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.journeyThumbnailUrl(imageUrls.get(0))
			.disclosure(request.getDisclosure())
			.introductionText(request.getIntroductionText())
			.numberOfPeople(request.getNumberOfPeople())
			.region(findByRegion(request.getRegion()))
			.build();

		journeyRepository.save(journey);
		log.info("createJourney() : 여정 생성 완료,  JourneyId : " + journey.getId());

		List<String> inputJourneyThemes = request.getJourneyThemes();
		for (String inputJourneyTheme : inputJourneyThemes) {
			JourneyTheme journeyTheme = JourneyTheme.builder()
				.journey(journey)
				.journeyThemeName(
					JourneyThemeType.findByTheme(inputJourneyTheme))
				.build();
			journeyThemeRepository.save(journeyTheme);
			log.info(
				"createJourney() : journeyTheme 생성 완료, journeyThemeId : "
					+ journeyTheme.getId());
		}

		log.info("createJourney() : 종료");
		return JourneyDto.CreateResponse.fromEntity(journey,
			inputJourneyThemes);
	}

	public List<JourneyDto.JourneyListResponse> list() {

		log.info("list() : 호출");

		List<JourneyDto.JourneyListResponse> list = new ArrayList<>();
		List<Journey> journeyList = journeyRepository
			.findAllByDisclosure("public");

		log.info("list() : 종료");

		return getList(list, journeyList);
	}

	public List<JourneyDto.JourneyListResponse> myList(String memberId) {

		log.info("myList() : 호출");

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new JourneyException(NOT_FOUND_MEMBER));

		List<JourneyDto.JourneyListResponse> list = new ArrayList<>();
		List<Journey> journeyList = journeyRepository
			.findAllByMember(member);

		log.info("myList() : 조회 완료");

		return getList(list, journeyList);
	}

	public List<Journey> filteredList(
		JourneySearchTheme journeySearchTheme) {

		log.info("filteredList() : 호출");

		log.info("filteredList() : 종료");
		return journeyRepositoryImpl.searchAll(journeySearchTheme);
	}

	private List<JourneyListResponse> getList(List<JourneyListResponse> list,
		List<Journey> journeyList) {

		log.info("getList() : 호출");

		for (Journey journey : journeyList) {
			List<String> journeyThemeTypeList = journeyThemeRepository
				.findAllByJourneyId(journey.getId())
				.stream().map(journeyTheme -> journeyTheme
					.getJourneyThemeName()
					.getThemeName())
				.collect(Collectors.toList());

			list.add(
				JourneyListResponse.fromEntity(journey, journeyThemeTypeList));
		}

		log.info("getList() : 종료");

		return list;
	}

	public DetailResponse journeyDetail(Long journeyId) {

		log.info("journeyDetail() : 호출");

		Journey journey = journeyRepository.findById(journeyId)
			.orElseThrow(() -> new JourneyException(NOT_FOUND_JOURNEY));

		List<String> journeyThemeTypeList = journeyThemeRepository
			.findAllByJourneyId(journey.getId())
			.stream().map(journeyTheme -> journeyTheme
				.getJourneyThemeName()
				.getThemeName())
			.collect(Collectors.toList());
		log.info(
			"journeyDetail() : journey 조회 완료, journeyId : " + journey.getId());

		log.info("journeyDetail() : 종료");

		return DetailResponse.fromEntity(journey, journeyThemeTypeList);
	}

	@Transactional
	public void deleteJourney(Long journeyId) {

		log.info("deleteJourney() : 호출");

		Journey journey = journeyRepository.findById(journeyId)
			.orElseThrow(() -> new JourneyException(NOT_FOUND_JOURNEY));

		List<JourneyTheme> journeyThemeTypeList = journeyThemeRepository
			.findAllByJourneyId(journey.getId());

		awsS3Service.deleteFile(journey.getJourneyThumbnailUrl());
		journeyThemeRepository.deleteAll(journeyThemeTypeList);
		journeyRepository.delete(journey);

		log.info("deleteJourney() : 여정 삭제 완료, journeyId : " + journey.getId());
		log.info("deleteJourney() : 종료");

	}

	@Transactional
	public UpdateResponse updateJourney(UpdateRequest request,
		MultipartFile thumbnail) {

		log.info("updateJourney() : 호출");

		Journey journey = journeyRepository.findById(request.getJourneyId())
			.orElseThrow(() -> new JourneyException(NOT_FOUND_JOURNEY));

		String email = journey.getMember().getEmail();
		if (!Objects.equals(email, request.getMemberId())) {
			throw new JourneyException(YOU_ARE_NOT_THE_AUTHOR);
		}

		awsS3Service.deleteFile(journey.getJourneyThumbnailUrl());
		List<String> imageUrls = awsS3Service.uploadFiles(
			Collections.singletonList(thumbnail));

		List<JourneyTheme> journeyThemes = journeyThemeRepository
			.findAllByJourneyId(journey.getId());

		journeyThemeRepository.deleteAll(journeyThemes);
		List<String> inputJourneyThemes = request.getJourneyThemes();
		for (String inputJourneyTheme : inputJourneyThemes) {
			JourneyTheme journeyTheme = JourneyTheme.builder()
				.journey(journey)
				.journeyThemeName(
					JourneyThemeType.findByTheme(inputJourneyTheme))
				.build();
			journeyThemeRepository.save(journeyTheme);
			log.info("updateJourney() : journeyTheme 수정 완료");
		}

		journey.setTitle(request.getTitle());
		journey.setStartDate(request.getStartDate());
		journey.setEndDate(request.getEndDate());
		journey.setNumberOfPeople(request.getNumberOfPeople());
		journey.setDisclosure(request.getDisclosure());
		journey.setIntroductionText(request.getIntroductionText());
		journey.setJourneyThumbnailUrl(imageUrls.get(0));
		journey.setRegion(findByRegion(request.getRegion()));
		log.info("updateJourney() : journey 수정 완료, journeyId : "
			+ journey.getId());

		log.info("updateJourney() : 종료");

		return JourneyDto.UpdateResponse
			.fromEntity(journey, inputJourneyThemes);
	}
}
