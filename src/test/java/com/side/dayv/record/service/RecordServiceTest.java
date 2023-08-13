package com.side.dayv.record.service;

import com.side.dayv.channel.dto.request.CreateChannelDto;
import com.side.dayv.channel.entity.Channel;
import com.side.dayv.channel.service.ChannelService;
import com.side.dayv.member.entity.Member;
import com.side.dayv.member.repository.MemberRepository;
import com.side.dayv.oauth.entity.ProviderType;
import com.side.dayv.record.dto.*;
import com.side.dayv.record.entity.Record;
import com.side.dayv.record.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class RecordServiceTest {

    @Autowired
    RecordService recordService;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChannelService channelService;

    Member MEMBER;

    Channel CHANNEL;

    CreateChannelDto CREATE_CHANNEL_REQUEST;

    RequestCreateRecordDTO CREATE_RECORD_REQUEST;

    @BeforeEach
    void init() {
        MEMBER = memberRepository.save(Member.builder()
                .email("csi@test.com")
                .birthday("1985-12-30")
                .nickname("csi")
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .provider(ProviderType.GOOGLE)
                .profileImageUrl("")
                .build());

        CREATE_CHANNEL_REQUEST = CreateChannelDto.builder()
                .name("테스트 채널")
                .secretYn(false)
                .build();

        CHANNEL = channelService.save(MEMBER.getId(), CREATE_CHANNEL_REQUEST);

        CREATE_RECORD_REQUEST = RequestCreateRecordDTO.builder()
                .title("일정 생성")
                .content("내용 테스트")
                .startDate(LocalDateTime.of(2023, 5, 1, 10, 30))
                .endDate(LocalDateTime.of(2023, 5, 30, 10, 30))
                .build();
    }

    /**
     *   case 1.
     *   |------|           (일정)
     *      |-------------| (검색)
     */
    @Test
    void 기간별_일정_조회_1(){
        RequestCreateRecordDTO createRecordDTO = RequestCreateRecordDTO.builder()
                .title("일정 생성")
                .content("내용 테스트")
                .startDate(LocalDateTime.of(2023, 4, 20, 00, 00))
                .endDate(LocalDateTime.of(2023, 5, 10, 10, 30))
                .build();

        ResponseRecordDTO record = recordService.createRecord(createRecordDTO, MEMBER.getId(), CHANNEL.getId());

        RequestSearchRecordDTO search = RequestSearchRecordDTO.builder()
                        .startDate(LocalDateTime.of(2023, 5, 1, 10, 29))
                        .endDate(LocalDateTime.of(2023, 5, 30, 12, 30))
                        .build();

        List<ResponseScheduleRecordDTO> list = recordService.getRecordOfSubscribedChannels(MEMBER.getId(), search);
        assertThat(list.size()).isEqualTo(1);
    }
    /**
     *   case 2.
     *         |------|     (일정)
     *      |-------------| (검색)
     */
    @Test
    void 기간별_일정_조회_2(){
        RequestCreateRecordDTO createRecordDTO = RequestCreateRecordDTO.builder()
                .title("일정 생성")
                .content("내용 테스트")
                .startDate(LocalDateTime.of(2023, 5, 1, 00, 00))
                .endDate(LocalDateTime.of(2023, 5, 10, 10, 30))
                .build();

        recordService.createRecord(createRecordDTO, MEMBER.getId(), CHANNEL.getId());

        RequestSearchRecordDTO search = RequestSearchRecordDTO.builder()
                .startDate(LocalDateTime.of(2023, 5, 1, 00, 00))
                .endDate(LocalDateTime.of(2023, 6, 1, 00, 00))
                .build();

        List<ResponseScheduleRecordDTO> list = recordService.getRecordOfSubscribedChannels(MEMBER.getId(), search);
        assertThat(list.size()).isEqualTo(1);
    }

    /**
     *   case 3.
     *                |------|   (일정)
     *      |-------------|      (검색)
     */
    @Test
    void 기간별_일정_조회_3(){
        RequestCreateRecordDTO createRecordDTO = RequestCreateRecordDTO.builder()
                .title("일정 생성")
                .content("내용 테스트")
                .startDate(LocalDateTime.of(2023, 5, 25, 10, 00))
                .endDate(LocalDateTime.of(2023, 6, 10, 10, 30))
                .build();

        recordService.createRecord(createRecordDTO, MEMBER.getId(), CHANNEL.getId());

        RequestSearchRecordDTO search = RequestSearchRecordDTO.builder()
                .startDate(LocalDateTime.of(2023, 5, 1, 00, 00))
                .endDate(LocalDateTime.of(2023, 6, 1, 00, 00))
                .build();

        List<ResponseScheduleRecordDTO> list = recordService.getRecordOfSubscribedChannels(MEMBER.getId(), search);
        assertThat(list.size()).isEqualTo(1);
    }

    /**
     *   case 4.
     *   |-------------------|   (일정)
     *      |-------------|      (검색)
     */
    @Test
    void 기간별_일정_조회_4(){
        RequestCreateRecordDTO createRecordDTO = RequestCreateRecordDTO.builder()
                .title("일정 생성")
                .content("내용 테스트")
                .startDate(LocalDateTime.of(2023, 4, 25, 10, 00))
                .endDate(LocalDateTime.of(2023, 6, 10, 10, 30))
                .build();

        recordService.createRecord(createRecordDTO, MEMBER.getId(), CHANNEL.getId());

        RequestSearchRecordDTO search = RequestSearchRecordDTO.builder()
                .startDate(LocalDateTime.of(2023, 5, 1, 00, 00))
                .endDate(LocalDateTime.of(2023, 6, 1, 00, 00))
                .build();

        List<ResponseScheduleRecordDTO> list = recordService.getRecordOfSubscribedChannels(MEMBER.getId(), search);
        assertThat(list.size()).isEqualTo(1);
    }


    @Test
    void 일정_등록() {
        ResponseRecordDTO record = recordService.createRecord(CREATE_RECORD_REQUEST, MEMBER.getId(), CHANNEL.getId());

        assertThat(record.getTitle()).isEqualTo(CREATE_RECORD_REQUEST.getTitle());
        assertThat(record.getContent()).isEqualTo(CREATE_RECORD_REQUEST.getContent());
        assertThat(record.getStartDate()).isEqualTo(CREATE_RECORD_REQUEST.getStartDate());
        assertThat(record.getEndDate()).isEqualTo(CREATE_RECORD_REQUEST.getEndDate());
        assertThat(record.isComplete()).isFalse();
    }

    @Test
    void 일정_등록실패_채널없음() {

    }

    @Test
    void 일정_등록실패_구독안된채널() {

    }

    @Test
    void 일정_등록실패_권한없음() {

    }

    @Test
    void 일정_삭제() {
        ResponseRecordDTO record = recordService.createRecord(CREATE_RECORD_REQUEST, MEMBER.getId(), CHANNEL.getId());
        recordService.removeRecord(MEMBER.getId(), record.getRecordId());

        Optional<Record> optional = recordRepository.findById(record.getRecordId());
        assertThat(optional.isEmpty()).isTrue();
    }

    @Test
    void 일정_삭제실패_이미삭제된일정() {

    }

    @Test
    void 일정_삭제실패_구독안된채널() {

    }

    @Test
    void 일정_삭제실패_권한없음() {

    }

    @Test
    void 일정_수정() {
        ResponseRecordDTO record = recordService.createRecord(CREATE_RECORD_REQUEST, MEMBER.getId(), CHANNEL.getId());

        RequestUpdateRecordDTO request = RequestUpdateRecordDTO.builder()
                .title("new title")
                .content("new content")
                .complete(true)
                .startDate(LocalDateTime.of(2023, 6, 1, 10, 30))
                .endDate(LocalDateTime.of(2023, 6, 30, 10, 30))
                .build();

        ResponseRecordDTO updateRecord = recordService.updateRecord(request, MEMBER.getId(), record.getRecordId());

        assertThat(updateRecord.getTitle()).isEqualTo(request.getTitle());
        assertThat(updateRecord.getContent()).isEqualTo(request.getContent());
        assertThat(updateRecord.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(updateRecord.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(updateRecord.isComplete()).isTrue();
    }

    @Test
    void 일정_수정실페_이미삭제된일정() {

    }

    @Test
    void 일정_수정실페_구독안된채널() {

    }

    @Test
    void 일정_수정실페_권한없음() {

    }
}