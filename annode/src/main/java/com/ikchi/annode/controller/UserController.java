package com.ikchi.annode.controller;

import com.ikchi.annode.annotation.JwtToUser;
import com.ikchi.annode.domain.dto.user.AnnodeFollowRes;
import com.ikchi.annode.domain.dto.user.FollowAcceptReq;
import com.ikchi.annode.domain.dto.user.FollowRes;
import com.ikchi.annode.domain.dto.user.LoginRes;
import com.ikchi.annode.domain.dto.user.LonginReq;
import com.ikchi.annode.domain.dto.user.MailAuthReq;
import com.ikchi.annode.domain.dto.user.PhoneInfo;
import com.ikchi.annode.domain.dto.user.PwResetInfoReq;
import com.ikchi.annode.domain.dto.user.SignUpReq;
import com.ikchi.annode.domain.dto.user.SmsAuthReq;
import com.ikchi.annode.domain.dto.user.UserInfo;
import com.ikchi.annode.domain.dto.user.UserSimpleRes;
import com.ikchi.annode.domain.entity.UserRelationShip;
import com.ikchi.annode.service.FileUploadService;
import com.ikchi.annode.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;


    @PostMapping("/phoneConfirm")
    public void handleMailConfirmRequest(@Validated @RequestBody SmsAuthReq smsAuthReq) {

        userService.handlePhoneAuthCode(smsAuthReq);

    }

    @PostMapping("/mailConfirm")
    public String handleMailConfirmRequest(@Validated @RequestBody MailAuthReq mailAuthReq) {

        userService.handleMailAuthCode(mailAuthReq);

        return "메일 인증번호가 전송되었습니다.";
    }


    @PostMapping("/signup")
    public String signUp(@Validated @ModelAttribute SignUpReq signUpReq) {

        // 유저를 가입시킨다 이때 프로필이미지는 파일이름으로 User에 넣어서 저장한다.
        userService.signUp(signUpReq);

        return "회원 가입이 완료되었습니다.";
    }


    @PostMapping("/passwordReset")
    public String passwordReset(@Validated @RequestBody PwResetInfoReq pwResetInfoReq)
        throws Exception {

        userService.passwordReset(pwResetInfoReq);

        return "비밀번호가 재설정 되었습니다.";
    }

    @PostMapping("/login")
    public LoginRes login(@RequestBody LonginReq request) {

        LoginRes loginRes = userService.login(request);
        return loginRes;
    }

    @PostMapping("/user/fcmToken")
    public UserSimpleRes fcmTokenCheck(@JwtToUser String email, @RequestBody String fcmToken) {

        UserSimpleRes userSimpleRes = userService.fcmTokenCheck(email, fcmToken);
        return userSimpleRes;
    }

    // Logout
    @PostMapping("/user/logout")
    public void logOut(@JwtToUser String email, @RequestBody String fcmToken) {

        userService.logOut(email, fcmToken);

    }


    // follow 신청을 수행
    @PostMapping("/user/follow")
    public void requestAnnodeFollow(@JwtToUser String email, @RequestParam String userIdentifier) {

        userService.requestAnnodeFollow(email, userIdentifier);

    }

    // follow 신청을 수행
    @PostMapping("/user/follow/delete")
    public void requestAnnodeUnFollow(@JwtToUser String email,
        @RequestParam String userIdentifier) {

        userService.unFollow(email, userIdentifier);

    }

    // Following 유저를 조회
    @GetMapping("/user/follow/one")
    public FollowRes getFollowInfo(@JwtToUser String email,
        @RequestParam String userIdentifier) {

        FollowRes annodeFollowList = userService.getFollowInfo(email, userIdentifier);

        return annodeFollowList;
    }

    // Follow 목록을 조회
    @GetMapping("/user/follow/list")
    public List<FollowRes> getFollowList(@JwtToUser String email) {

        List<FollowRes> annodeFollowList = userService.getFollowList(email);

        return annodeFollowList;
    }

    // Cross Follow중인 팔로잉 유저 List를 반환
    @GetMapping("/user/follow/cross/list")
    public List<UserSimpleRes> getCrossFollowList(@JwtToUser String email) {

        List<UserRelationShip> crossFollowsByUser = userService.getCrossFollowsByUser(email);
        List<UserSimpleRes> userSimpleResList = userService.userRelationshipsToSimpleUserInfos(
            crossFollowsByUser);

        return userSimpleResList;
    }


    // 유저가 요청받은 follow 목록을 조회
    @GetMapping("/user/follow/request/list")
    public List<AnnodeFollowRes> getAnnodeFollow(@JwtToUser String email) {

        List<AnnodeFollowRes> annodeFollowList = userService.getAnnodeFollowList(email);

        return annodeFollowList;
    }


    // 유저에게 제안된 follow 수락 거절을 수행
    @PostMapping("/user/follow/accept")
    public void acceptFollowRequest(@JwtToUser String email,
        @RequestBody FollowAcceptReq followAcceptReq) {

//        어노드팔로우요청은 수락이됬든 거절이든 제거하고
//        수락시 친구테이블에 레코드 추가

        userService.acceptFollowRequest(email, followAcceptReq);

    }


    // 회원 탈퇴
    @DeleteMapping("/user/delete")
    public void userDelete(@JwtToUser String userMail) {
        userService.deleteUser(userMail);
    }

    // 회원 탈퇴
    @PostMapping("/admin/user/ban")
    public void userBan(@JwtToUser String userMail, String targetIdentifier) {
        userService.userBan(userMail, targetIdentifier);
    }


    @GetMapping("/user/info")
    public UserInfo getUserInfo(@JwtToUser String userMail,
        @RequestParam(required = false) String userIdentifier) {

        UserInfo userInfo = null;
        if (userIdentifier != null) {
            userInfo = userService.getUserInfo(userMail, userIdentifier);
        } else {
            userInfo = userService.getUserMyInfo(userMail);
        }

        return userInfo;
    }


    @GetMapping("/user/search")
    public UserSimpleRes getUserSearch(@RequestParam String userSearchInput) {
        UserSimpleRes userInfo = userService.getUserSearch(userSearchInput);

        return userInfo;
    }

    @PostMapping("/user/phone/list")
    public List<UserSimpleRes> getUserListByPhone(@RequestBody List<PhoneInfo> phoneInfoList) {

        List<UserSimpleRes> userInfoList = userService.getUserListByPhone(phoneInfoList);

        return userInfoList;
    }


    //  multipart/form-data의 요청바디에서 일부를 추출
    @PostMapping(value = "/user/info/update")
    public String userInfoUpdate(@JwtToUser String userMail,
        @RequestParam(name = "nickName", required = false) String nickName,
        @RequestParam(name = "profileImgFile", required = false) MultipartFile profileImgFile) {

        userService.userUpdate(userMail, nickName, profileImgFile);

        return "성공적으로 정보가 수정되었습니다.";
    }


    @GetMapping("/admin/test")
    public ResponseEntity<String> userTest() {
        return new ResponseEntity<>("인가 성공", HttpStatus.OK);
    }


}
