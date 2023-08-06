package com.ikchi.annode.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.ikchi.annode.domain.entity.AnnodeFollow;
import com.ikchi.annode.domain.entity.BanInfo;
import com.ikchi.annode.domain.entity.ReportUserAndPospace;
import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.domain.entity.UserRelationShip;
import com.ikchi.annode.domain.vo.Authority;
import com.ikchi.annode.repository.PospaceRepository;
import com.ikchi.annode.repository.ReportUserRepository;
import com.ikchi.annode.repository.UserRepository;
import com.ikchi.annode.security.JwtProvider;
import com.ikchi.annode.service.Util.user.MailAuthManager;
import com.ikchi.annode.service.Util.user.RegisterMailService;
import com.ikchi.annode.service.Util.user.SmsService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Transactional
@Service
@RequiredArgsConstructor
public class UserService {


    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ReportUserRepository reportUserRepository;
    private final PospaceRepository pospaceRepository;

    private final FileUploadService fileUploadService;

    private final SocketHandlerService socketHandlerService;

    private final RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valOps;


    @PostConstruct
    private void init() {
        valOps = redisTemplate.opsForValue();
    }

    private final SmsService smsService;

    // 메일관련 서비스
    private final RegisterMailService registerMail;
    // 서버의 메모리에서 mail 인증 코드를 관리
    private final MailAuthManager mailAuthManager;

    // 인증번호 유효시간 (분)
    @Value("${app.codeExpirationMinutes}")
    private int CODE_EXPIRATION_MINUTES;

    @Value("${app.invitationSuccess}")
    private int invitationSuccess;

    @Value("${admin.signUpPassword}")
    private String adminSignUpPassword;

    @Value("${default.profileimg}")
    private String defaultProfileImg;

    // 인증번호 요청이 오게되면 발동하는 핸들러 메소드
    public void handlePhoneAuthCode(SmsAuthReq smsAuthReq) {

        String eventName = smsAuthReq.getEventName();
        String phoneNumber = smsAuthReq.getPhoneNumber();

        // email 전송 및 인증정보 객체 저장
        storePhoneAuthInfo(eventName, phoneNumber);
    }

    public void storePhoneAuthInfo(String eventName, String phoneNumber) {

        // 인증번호 event에 따라 구분하여 생성한 인증정보객체를 mailAuthManager에서 관리시킨다
        // mailAuthManager를 통해 서버 메모리에 유저의 메일을 key로 인증정보객체를 저장시킨다
        switch (eventName) {
            case "signUp": {
                Optional<User> findUser = userRepository.findUserByPhone(phoneNumber);
                if (findUser.isPresent()) {
                    throw new IllegalStateException(
                        "이미 가입되어있는 전화번호입니다 : " + findUser.get().getPhoneNumber());
                }

                // 수신자 메일에게 보낼 인증 코드 전송 및 Redis를 통한 분산캐시
                String authCode = generateAndSendPhoneAuthCode(phoneNumber);
                System.out.println("로그, phone 인증번호 dlswmdqjsgh = " + authCode);
                valOps.set(phoneNumber, authCode, 5, TimeUnit.MINUTES);

            }
            break;
            default: {
                System.out.println("로그, 올바르지 않은 이벤트명 입니다.");
            }
        }
    }


    // 인증번호 요청이 오게되면 발동하는 핸들러 메소드
    public void handleMailAuthCode(MailAuthReq mailAuthReq) {

        String eventName = mailAuthReq.getEventName();
        String email = mailAuthReq.getEmail();

        // email 전송 및 인증정보 객체 저장
        storeMailAuthInfo(eventName, email);
    }


    // 인증번호를 만료시간 정보를 포함한 EmailAuthInfo 객체로 만들어서 서버 메모리에 저장
    public void storeMailAuthInfo(String eventName, String email) {

        // 인증번호 event에 따라 구분하여 생성한 인증정보객체를 mailAuthManager에서 관리시킨다
        // mailAuthManager를 통해 서버 메모리에 유저의 메일을 key로 인증정보객체를 저장시킨다
        switch (eventName) {
            case "signUp": {
                Optional<User> findUser = userRepository.findUserByMail(email);
                if (findUser.isPresent()) {
                    throw new IllegalStateException(
                        "이미 가입되어있는 email입니다 : " + findUser.get().getEmail());
                }

                // 수신자 메일에게 보낼 인증 코드 전송 및 Redis를 통한 분산캐시
                String authCode = generateAndSendMailAuthCode(email);
                valOps.set(email, authCode, 5, TimeUnit.MINUTES);

            }
            break;
            case "resetPassword": {
                Optional<User> userByMail = userRepository.findUserByMail(email);
                if (!userByMail.isPresent()) {
                    throw new IllegalStateException(
                        "가입되어있지 않은 email입니다 : " + userByMail.get().getEmail());
                }

                // 수신자 메일에게 보낼 인증 코드 전송 및 Redis를 통한 분산캐시
                String authCode = generateAndSendMailAuthCode(email);
                valOps.set(email, authCode, 5, TimeUnit.MINUTES);

            }
            break;
            case "accountRemove": {

                Optional<User> userByMail = userRepository.findUserByMail(email);

                if (!userByMail.isPresent()) {
                    throw new IllegalStateException(
                        "가입되어있지 않은 email입니다 : " + userByMail.get().getEmail());
                }

                // 수신자 메일에게 보낼 인증 코드 전송 및 Redis를 통한 분산캐시
                String authCode = generateAndSendMailAuthCode(email);
                valOps.set(email, authCode, 5, TimeUnit.MINUTES);

            }
            break;
            default: {
                System.out.println("로그, 올바르지 않은 이벤트명 입니다.");
            }
        }
    }

    // 인증번호 생성 및 전송
    public String generateAndSendMailAuthCode(String email) {
        // 인증번호를 만들어서 수신자 메일에 전송하고 인증번호를 반환한다
        return registerMail.sendSimpleMessage(email)
            .orElseThrow(() -> new IllegalStateException("인증 메일 전송에 실패했습니다"));
    }

    // 인증번호 생성 및 전송
    public String generateAndSendPhoneAuthCode(String phoneNumber) {
        // 인증번호를 만들어서 수신자 메일에 전송하고 인증번호를 반환한다
        return smsService.sendSms(phoneNumber);

    }


    // 신규회원가입자가 생기면 어드민에게 알림
    public void userEventMailSend(User user, String eventName) {

        String nickName = user.getNickName();
        String email = user.getEmail();
        registerMail.sendUserEventMailMessage(nickName, email, eventName);
    }


    @Transactional
    public void signUp(SignUpReq signUpReq) {

        // admin
        if (signUpReq.getPassword().equals(adminSignUpPassword)) {
            // user을 생성한다 이때 기본 권한은 ROLE_USER로 설정한다

            User user = new User(signUpReq.getEmail(),
                passwordEncoder.encode(signUpReq.getPassword()), signUpReq.getPhoneNumber(),
                signUpReq.getNickName(), signUpReq.isAccountPublic());
            user.setProfileImgFileUrl(defaultProfileImg);

            user.adminSetting();
            userRepository.save(user);
            return;
        }

        String email = signUpReq.getEmail();
        String phoneNumber = signUpReq.getPhoneNumber();

        String mailAuthCode = valOps.get(email);
        String phoneAuthCode = valOps.get(phoneNumber);

        // 유효성 검증 진행후 모두 통과시 회원가입 진행
        // 만료시간 , 악성유저(밴) , 중복가입 , 인증번호 일치  검사 여부 검사

        // Redis에서 조회한 코드가 존재하는지 만료시간 검사
        if (mailAuthCode != null && phoneAuthCode != null) {

            Boolean isBanUser = userRepository.banUserCheck(signUpReq.getPhoneNumber(),
                signUpReq.getEmail());
            if (isBanUser) {
                throw new IllegalArgumentException("회원가입 불가한 유저입니다");
            }

            Optional<User> findUserByEmail = userRepository.findUserByMail(email);
            if (findUserByEmail.isPresent()) {
                throw new IllegalStateException(
                    "이미 가입되어있는 이메일입니다 : " + findUserByEmail.get().getEmail());
            }

            Optional<User> findUserByPhone = userRepository.findUserByPhone(phoneNumber);
            if (findUserByPhone.isPresent()) {
                throw new IllegalStateException(
                    "이미 가입되어있는 PhoneNumber입니다 : " + findUserByPhone.get().getPhoneNumber());
            }

            if (!mailAuthCode.equals(signUpReq.getMailAuthCode())) {
                throw new IllegalArgumentException("Email 코드가 일치하지 않습니다");
            }

            if (!phoneAuthCode.equals(signUpReq.getPhoneAuthCode())) {
                throw new IllegalArgumentException("PhoneNumber 코드가 일치하지 않습니다");
            }

            int nickNameLength = signUpReq.getNickName().length();
            if (nickNameLength < 2 || nickNameLength > 10) {
                throw new IllegalArgumentException("닉네임은 2자리 이상 10자리 이하로 입력해주세요.");
            }

            // 비밀번호는 8자리 이상이며 영문자, 숫자를 포함해야 한다
            if (!signUpReq.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@!#*?]{8,}$")) {
                throw new IllegalArgumentException("비밀번호를 다시 확인해주세요");
            }

            // user을 생성한다 이때 기본 권한은 ROLE_USER로 설정한다
            User user = new User(signUpReq.getEmail(),
                passwordEncoder.encode(signUpReq.getPassword()), signUpReq.getPhoneNumber(),
                signUpReq.getNickName(), signUpReq.isAccountPublic());

            String profileImgFileUrl = defaultProfileImg;
            if (signUpReq.getProfileImageFile() != null) {

                List<MultipartFile> imageFiles = Collections.singletonList(
                    signUpReq.getProfileImageFile());

                List<String> singleImgName = fileUploadService.storeFiles(imageFiles,
                    user.getUserIdentifier());

                profileImgFileUrl = singleImgName.get(0);
            }

            user.setProfileImgFileUrl(profileImgFileUrl);

            // 신규유저 저장
            userRepository.save(user);

            System.out.println("로그, 신규 회원가입 user = " + user);

            // 회원가입 완료후 mailAuthManager에서 관리되는 가입한 유저의 인증정보객체를 삭제한다
            redisTemplate.delete(email);
            redisTemplate.delete(phoneNumber);

        } else {
            throw new IllegalStateException("인증번호가 유효하지않습니다.");
        }
    }


    // 유저의 follow중인 유저목록 조회
    public List<FollowRes> getFollowList(String email) {

        User user = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        List<UserRelationShip> userRelationShipList = userRepository.findUserRelationShipList(user);

        List<FollowRes> friendRes = userRelationShipList.stream().map(userRelationShip -> {
            User friendEntity = userRelationShip.getFollowingUser();
            FollowRes result = new FollowRes(friendEntity.getNickName(),
                friendEntity.getUserIdentifier(), friendEntity.getProfileImgFileUrl());

            return result;
        }).collect(Collectors.toList());

        return friendRes;
    }

    // 유저와 타겟유저를 받아서 팔로우관계인지 확인
    public boolean getFollowUserCheck(User user, User targetUser) {

        Optional<UserRelationShip> userRelationShip = userRepository.findUserRelationShip(
            user,
            targetUser);

        if (!userRelationShip.isPresent()) {
            return false;
        }

        return true;
    }


    @Transactional
    public List<UserRelationShip> getCrossFollowsByUser(String email) {

        User targetUser = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        List<UserRelationShip> followedRelationShipList = userRepository.findUserRelationShipList(
            targetUser);

        List<UserRelationShip> followerRelationShipList = userRepository.findFollowerRelationShipList(
            targetUser);

        List<User> usersFollowingTarget = followerRelationShipList.stream()
            .map(UserRelationShip::getUser).collect(Collectors.toList());

        List<UserRelationShip> crossFollowList = followedRelationShipList.stream()
            .filter(relation -> usersFollowingTarget.contains(relation.getFollowingUser())).collect(
                Collectors.toList());

        return crossFollowList;
    }


    // 유저관계List를 받아서 팔로잉중인 유저들을 추출후 List로 반환
    public List<UserSimpleRes> userRelationshipsToSimpleUserInfos(
        List<UserRelationShip> userRelationShipList) {

        List<UserSimpleRes> userSimpleResList = userRelationShipList.stream().map(item -> {
            User followingUser = item.getFollowingUser();
            UserSimpleRes userSimpleRes = new UserSimpleRes(followingUser.getUserIdentifier(),
                followingUser.getNickName(), followingUser.getProfileImgFileUrl());

            return userSimpleRes;
        }).collect(Collectors.toList());

        return userSimpleResList;
    }

    public User getUserByIdentifier(String userIdentifier) {

        User user = userRepository.findUserByIdentifier(userIdentifier)
            .orElseThrow(() -> new NoSuchElementException("존재하지않는 유저입니다"));

        return user;
    }

    public FollowRes getFollowInfo(String email, String userIdentifier) {

        User user = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        User identifierUser = userRepository.findUserByIdentifier(userIdentifier)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

//        UserRelationship userRelationshipEntity = userRepository.findFriend(user, identifierUser)
//            .orElseThrow(() -> new NoSuchElementException("유효하지않은 친구입니다"));
//
//        User friend = userRelationshipEntity.getFollowUser();
//        FollowRes res = new FollowRes(friend.getNickName(),
//            friend.getUserIdentifier(), friend.getprofileImgFileUrl());

        return null;
    }


    // Follow 신청을 유효성 검사를 통과후 수행 - Follow 수신자의 계정 공개여부에 따라 즉시성사 체크
    public void requestAnnodeFollow(String email, String userIdentifier) {

        User requester = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        User receiver = userRepository.findUserByIdentifier(userIdentifier)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        if (requester.equals(receiver)) {
            throw new IllegalStateException("자신을 팔로워할수 없습니다");
        }

        List<AnnodeFollow> annodeFollowList = userRepository.findAnnodeFollowByRequester(requester);
        List<UserRelationShip> requesterRelationShipList = userRepository.findUserRelationShipList(
            requester);

        boolean isDoubleFollowReq = annodeFollowList.stream()
            .anyMatch(followReq -> followReq.getReceiver().equals(receiver));

        boolean isAlreadyFollow = requesterRelationShipList.stream()
            .anyMatch(relationShip -> relationShip.getFollowingUser().equals(receiver));

        if (isDoubleFollowReq) {
            throw new IllegalArgumentException("이미 같은 어노드에게 Follow 요청을 보냈습니다");
        }

        if (isAlreadyFollow) {
            throw new IllegalArgumentException("이미 " + receiver.getNickName() + "님과 Follow 관계입니다");
        }

        // 만약 follow 수신자의 계정이 공개계정이라면 즉시 친구추가시키기
        if (receiver.isAccountPublic()) {
            UserRelationShip userRelationship = new UserRelationShip(requester, receiver);
            requester.addUserRelationShip(userRelationship);
            return;
        }

        AnnodeFollow annodeFollow = new AnnodeFollow(requester, receiver);

        userRepository.saveAnnodeFollow(annodeFollow);

        notificationService.sendNotification(receiver.getFcmToken(),
            requester.getNickName() + "님에게 follow 요청을 받으셨습니다", null);
    }

    // unFollow
    public void unFollow(String email, String userIdentifier) {

        User requester = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        User receiver = userRepository.findUserByIdentifier(userIdentifier)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        List<UserRelationShip> requesterRelationShipList = userRepository.findUserRelationShipList(
            requester);

        Optional<UserRelationShip> userRelationShip = requesterRelationShipList.stream()
            .filter(relationShip -> relationShip.getFollowingUser().equals(receiver))
            .findFirst();

        if (!userRelationShip.isPresent()) {
            throw new IllegalArgumentException(receiver.getNickName() + "님과 Follow 관계가 아닙니다");
        }

        userRepository.deleteUserRelationShip(userRelationShip.get());
    }

    // 유저에게 제안된 follow 목록 조회
    public List<AnnodeFollowRes> getAnnodeFollowList(String email) {

        User receiver = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        List<AnnodeFollow> annodeFollowList = userRepository.findAnnodeFollowByReceiver(
            receiver);

        List<AnnodeFollowRes> annodeFollowResList = annodeFollowList.stream().map(annodeFollow -> {
            User requester = annodeFollow.getRequester();
            AnnodeFollowRes annodeFollowRes = new AnnodeFollowRes(requester.getNickName(),
                requester.getProfileImgFileUrl(), requester.getUserIdentifier(),
                annodeFollow.getId());

            return annodeFollowRes;
        }).collect(Collectors.toList());

        return annodeFollowResList;
    }

    // 유저에게 제안된 follow 수락 거절을 수행
    public void acceptFollowRequest(String email, FollowAcceptReq followAcceptReq) {

        User receiver = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        AnnodeFollow annodeFollow = userRepository.findAnnodeFollow(
                followAcceptReq.getAnnodeFollowId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 Follow 요청입니다"));

        User requester = annodeFollow.getRequester();

        List<UserRelationShip> userRelationShipList = requester.getUserRelationShipList();

        boolean isAlreadyFollowing = userRelationShipList.stream()
            .anyMatch(relation -> relation.getFollowingUser().equals(receiver));

        if (isAlreadyFollowing) {
            throw new IllegalArgumentException("이미 " + requester.getNickName() + "님과 Follow 관계입니다");
        }

        if (followAcceptReq.getAccept()) {

            UserRelationShip userRelationship = new UserRelationShip(requester, receiver);
            requester.addUserRelationShip(userRelationship);

            notificationService.sendNotification(requester.getFcmToken(),
                receiver.getNickName() + "님과 follow관계가 되었습니다!", null);
        }

        userRepository.deleteAnnodeFollow(annodeFollow);
    }

    public List<UserRelationShip> getFollowerRelationShipList(String email) {

        User targetUser = userRepository.findUserByMail(email)
            .orElseThrow(() -> new NoSuchElementException("찾을수 없는 유저입니다"));

        List<UserRelationShip> followerRelationShipList = userRepository.findFollowerRelationShipList(
            targetUser);

        return followerRelationShipList;
    }


    // 비밀번호 재설정을 검증통과시 변경하도록하는 메소드
    // 검증은 메일 기본형식 , 가입되지 않은 메일 , 인증번호 유효성 검사
    @Transactional
    public void passwordReset(PwResetInfoReq pwResetInfoReq) {

        String email = pwResetInfoReq.getEmail();

        String mailAuthCode = valOps.get(email);

        // 유효성 검증 진행후 모두 통과시 회원가입 진행
        // 만료시간 , 중복가입 , 인증번호 일치 여부 검사
        if (mailAuthCode != null) {

            User user = userRepository.findUserByMail(email)
                .orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일입니다."));

            // 서버에서는 mailAuthCode를 암호화하여 저장하기때문에 서버에 저장된 mailAuthCode 비교시 matches() 메소드를 사용한다.
            if (!mailAuthCode.equals(pwResetInfoReq.getMailAuthCode())) {
                throw new IllegalArgumentException("이메일 인증번호가 일치하지 않습니다");
            }

            // 비밀번호는 8자리 이상이며 영문자, 숫자를 포함해야 한다
            if (!pwResetInfoReq.getResetPassword()
                .matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@!#*?]{8,}$")) {
                throw new IllegalArgumentException("비밀번호를 다시 확인해주세요");
            }

            user.setPassword(passwordEncoder.encode(pwResetInfoReq.getResetPassword()));
            redisTemplate.delete(email);
        } else {
            throw new IllegalStateException("인증번호가 유효하지않습니다.");
        }
    }


    @Transactional
    public LoginRes login(LonginReq request) {

        User user = userRepository.findUserByMail(request.getEmail())
            .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

        boolean isAdmit = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        // 스프링 시큐리티의 암호화된 비밀번호 유효성 검사
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()) && !isAdmit) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 로그인 시 토큰을 발급한다.
        String token = jwtProvider.createToken(user.getEmail(), user.getRoles());
        List<Authority> roles = user.getRoles();

        LoginRes loginRes = new LoginRes(token, roles);

        return loginRes;
    }

    @Transactional
    public UserSimpleRes fcmTokenCheck(String email, String fcmToken) {

        UserSimpleRes userSimpleRes = null;
        try {

            User user = userRepository.findUserByMail(email)
                .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(fcmToken, Map.class);
            String fcmTokenValue = map.get("fcmToken");

            if (fcmTokenValue != null && !fcmTokenValue.isEmpty() && !fcmTokenValue.isBlank()) {

                if (user.getFcmToken() == null || !user.getFcmToken().equals(fcmTokenValue)) {
                    user.setFcmToken(fcmTokenValue);
                }
            }
            userSimpleRes = new UserSimpleRes(user.getUserIdentifier(), user.getNickName(),
                user.getProfileImgFileUrl());

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return userSimpleRes;

    }


    public User getUserByEmail(String mail) {
        User user = userRepository.findUserByMail(mail)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));
        return user;
    }


    public UserInfo getUserInfo(String userMail, String userIdentifier) {

        User targetUser = userRepository.findUserByIdentifier(userIdentifier)
            .orElseThrow(() -> new NoSuchElementException("존재하지않는 유저입니다"));

        List<UserRelationShip> followerRelationShipList = getFollowerRelationShipList(
            targetUser.getEmail());

        UserInfo userInfo = new UserInfo(targetUser.getEmail(), targetUser.getNickName(),
            targetUser.getUserIdentifier(),
            targetUser.getProfileImgFileUrl(), targetUser.getUserCreatedDate(),
            followerRelationShipList.size());

        // user가 비공개 계정인경우 팔로우되어있는지 확인하고

        if (userMail != null) {

            User user = userRepository.findUserByMail(userMail)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다"));

            boolean isFollowing = followerRelationShipList.stream().anyMatch(item ->
                item.getUser().equals(user));

            // follow 여부를 확인하고 맞다면 이미 follow 관계임을 응답
            if (isFollowing) {
                userInfo.setFollowRelationShip(true);
            }

        }

        return userInfo;
    }

    public UserInfo getUserMyInfo(String userMail) {

        User user = userRepository.findUserByMail(userMail)
            .orElseThrow(() -> new NoSuchElementException("존재하지않는 유저입니다"));

        List<UserRelationShip> followerRelationShipList = getFollowerRelationShipList(
            user.getEmail());

        UserInfo userMyInfo = new UserInfo(user.getEmail(), user.getNickName(),
            user.getUserIdentifier(),
            user.getProfileImgFileUrl(), user.getUserCreatedDate(),
            followerRelationShipList.size());

        return userMyInfo;
    }

    public UserSimpleRes getUserSearch(String userSearchInput) {

        User targetUser = userRepository.findUserByIdentifierOrEmail(userSearchInput)
            .orElseThrow(() -> new NoSuchElementException("존재하지않는 유저입니다"));

        UserSimpleRes userSimpleRes = new UserSimpleRes(targetUser.getUserIdentifier(),
            targetUser.getNickName(), targetUser.getProfileImgFileUrl());

        return userSimpleRes;
    }

    @Transactional
    public List<UserSimpleRes> getUserListByPhone(List<PhoneInfo> phoneInfoList) {

        List<String> phoneNumberList = phoneInfoList.stream().map(PhoneInfo::getPhoneNumber)
            .collect(Collectors.toList());

        List<User> userList = userRepository.findUserListByPhone(phoneNumberList);

        List<UserSimpleRes> userSimpleResList = userList.stream().map(
            user -> new UserSimpleRes(user.getUserIdentifier(), user.getNickName(),
                user.getProfileImgFileUrl())).collect(Collectors.toList());

        return userSimpleResList;
    }

    // 유저의 정보를 수정한다 이때 유저 프로필 이미지가 변경된다면 새 이미지를 저장시킨뒤 기존 이미지를 삭제하고 user의 profileImgFileUrl을 변경시킨다
    public void userUpdate(String userMail, String nickName, MultipartFile profileImgFile) {
        User user = userRepository.findUserByMail(userMail)
            .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

        // userUpdate 중 null이 아닌값이 있으면 그값을 user의 setter로 교체
        if (profileImgFile != null) {

            // 유저의 기본 프로필 이미지가 아닐때만 이미지를 삭제한다
            if (!user.getProfileImgFileUrl().equals(defaultProfileImg)) {
                List<String> deleteSingleImgUrlList = new ArrayList<>();
                deleteSingleImgUrlList.add(user.getProfileImgFileUrl());
                fileUploadService.deleteImgFile(deleteSingleImgUrlList);
            }

            List<MultipartFile> imageFiles = new ArrayList<>();
            imageFiles.add(profileImgFile);

            List<String> fileNames = fileUploadService.storeFiles(imageFiles,
                user.getUserIdentifier());
            String newprofileImgFileUrl = fileNames.get(0);

            user.setProfileImgFileUrl(newprofileImgFileUrl);
        }

        if (nickName != null && !nickName.isBlank()) {

            if (nickName.length() > 20 || nickName.length() < 2) {

                throw new NoSuchElementException("올바른 닉네임 2자리 이상 20자리 이하로 입력해주세요.");
            }
            user.setNickName(nickName);
        }

    }


    public void deleteUser(String userMail) {
        User user = userRepository.findUserByMail(userMail)
            .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

        if (user.getJoinedRoom() != null) {
            throw new IllegalStateException("참여중인 대화방에서 나간뒤 계정탈퇴를 시도하세요");
        }

        // 유저의 기본 프로필 이미지가 아닐때만 이미지를 삭제한다
        if (!user.getProfileImgFileUrl().equals(defaultProfileImg)) {

            List<String> deleteSingleImgUrlList = Collections.singletonList(
                user.getProfileImgFileUrl());

            fileUploadService.deleteImgFile(deleteSingleImgUrlList);
        }

        deleteRelationShips(user);
        deleteReports(user);
        deletePospaceUserTagList(user);
        deleteAnnodeFollowByUser(user);

        userRepository.delete(user);
    }

    public void userBan(String userMail, String targetIdentifier) {
        User admin = userRepository.findUserByMail(userMail)
            .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("요청할수 있는 권한이 없습니다");
        }

        User user = userRepository.findUserByIdentifier(targetIdentifier)
            .orElseThrow(() -> new NoSuchElementException("가입되지 않은 E-MAIL 입니다."));

        // 유저의 기본 프로필 이미지가 아닐때만 이미지를 삭제한다
        if (!user.getProfileImgFileUrl().equals(defaultProfileImg)) {

            List<String> deleteSingleImgUrlList = Collections.singletonList(
                user.getProfileImgFileUrl());

            fileUploadService.deleteImgFile(deleteSingleImgUrlList);
        }

        deleteRelationShips(user);
        deleteReports(user);
        deletePospaceUserTagList(user);
        deleteAnnodeFollowByUser(user);

        userRepository.delete(user);

        BanInfo banInfo = new BanInfo(user.getEmail(), user.getPhoneNumber());
        userRepository.banInfoAdd(banInfo);
    }

    public void deleteRelationShips(User user) {
        List<UserRelationShip> userRelationShipList = userRepository.findUserRelationShipList(user);
        List<UserRelationShip> followerRelationShipList = userRepository.findFollowerRelationShipList(
            user);

        userRelationShipList.forEach(
            userRelationShip -> userRepository.deleteUserRelationShip(userRelationShip));
        followerRelationShipList.forEach(
            userRelationShip -> userRepository.deleteUserRelationShip(userRelationShip));
    }

    public void deleteReports(User user) {
        List<ReportUserAndPospace> reportUserAndPospaceListByUser = reportUserRepository.findReportUserAndPospaceListByUser(
            user);

        reportUserAndPospaceListByUser.forEach(
            reportUserAndPospace -> reportUserRepository.delete(reportUserAndPospace));

    }

    public void deletePospaceUserTagList(User user) {
        pospaceRepository.removeUserTagFromPospace(user);
    }

    public void deleteAnnodeFollowByUser(User user) {

        List<AnnodeFollow> annodeFollowList = userRepository.findAnnodeFollowByReceiver(user);
        List<AnnodeFollow> annodeFollowByRequester = userRepository.findAnnodeFollowByRequester(
            user);

        annodeFollowList.forEach(annodeFollow -> userRepository.deleteAnnodeFollow(annodeFollow));
        annodeFollowByRequester.forEach(
            annodeFollow -> userRepository.deleteAnnodeFollow(annodeFollow));
    }

    @Transactional
    public void logOut(String email, String fcmToken) {

        try {
            User user = userRepository.findUserByMail(email)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다"));

            // fcm토큰을 가진 기기에서 로그아웃 수행시 null로 변경

            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> fcmTokenMap = mapper.readValue(fcmToken, Map.class);

            String fcmTokenValue = fcmTokenMap.get("fcmToken");
            System.out.println("로그아웃 fcmTokenValue = " + fcmTokenValue);

            if (fcmTokenValue != null && !fcmTokenValue.isEmpty() && !fcmTokenValue.isBlank()) {
                user.setFcmToken(null);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}


