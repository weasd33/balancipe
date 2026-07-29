package com.beokay.balancipe.user.service;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.user.domain.Gender;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.dto.UserProfileResponse;
import com.beokay.balancipe.user.dto.UserProfileUpdateRequest;
import com.beokay.balancipe.user.fixture.UserProfileUpdateRequestFixture;
import com.beokay.balancipe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 내_프로필을_조회한다() {
        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getMyProfile(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("hello@gmail.com");
        assertThat(response.nickname()).isEqualTo("홍길동");
        assertThat(response.gender()).isEqualTo(Gender.MALE);
        assertThat(response.birthYear()).isEqualTo(2000);
    }

    @Test
    void 존재하지_않는_사용자를_조회하면_예외를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void 프로필을_수정하면_닉네임과_이미지가_변경된다() {
        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserProfileUpdateRequest request = UserProfileUpdateRequestFixture.VALID_UPDATE_REQUEST.getRequest();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);

        UserProfileResponse response = userService.updateMyProfile(1L, request);

        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.profileImageUrl()).isEqualTo(request.profileImageUrl());
    }

    @Test
    void 본인의_현재_닉네임과_동일한_값으로_수정하면_중복_체크를_건너뛴다() {
        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("홍길동", "https://cdn.balancipe.com/profile/1.png");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.updateMyProfile(1L, request);

        assertThat(response.nickname()).isEqualTo("홍길동");
    }

    @Test
    void 다른_사용자의_닉네임과_중복되면_예외를_던진다() {
        User user = User.create("hello@gmail.com", "encoded-password", "홍길동", Gender.MALE, 2000);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserProfileUpdateRequest request = UserProfileUpdateRequestFixture.VALID_UPDATE_REQUEST.getRequest();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(request.nickname())).willReturn(true);

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }
}
