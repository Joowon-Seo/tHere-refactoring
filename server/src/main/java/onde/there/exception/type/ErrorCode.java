package onde.there.exception.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	NOT_FOUND_JOURNEY("존재하지 않는 여정입니다."),
	NOT_FOUND_PLACE("존재하지 않는 장소입니다."),
	MISMATCH_PLACE_CATEGORY_TYPE("일치하는 카테고리 타입이 없습니다."),
	BAD_REQUEST("입력 값을 확인해 주세요!"),
	MEMBER_NOT_FOUND("등록 되지 않은 아이디 입니다"),
	PASSWORD_MISMATCH("비밀 번호가 일치하지 않습니다"),
	DUPLICATED_MEMBER_EMAIL("중복된 이메일 입니다!"),
	DUPLICATED_MEMBER_ID("중복된 아이디 입니다!"),
	EMAIL_AUTH_REQUIRED("이메일 인증이 완료되지 않았습니다!"),
	DELETED_NOTING("이 여정에는 삭제할 장소가 없습니다."),
	DATE_ERROR("잘못된 날짜입니다."),
	THERE_IS_NO_MATCHING_THEME("일치하는 테마가 없습니다.")
	;
	private final String description;
}
