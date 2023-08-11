package com.ikchi.annode.service;


import com.ikchi.annode.Enum.Constants.Visibility;
import com.ikchi.annode.Enum.PospaceExceptionMessage;
import com.ikchi.annode.Enum.UserExceptionMessage;
import com.ikchi.annode.domain.dto.ReportReq;
import com.ikchi.annode.domain.dto.pospace.PagePospaceRes;
import com.ikchi.annode.domain.dto.pospace.PospaceCommentCreateReq;
import com.ikchi.annode.domain.dto.pospace.PospaceCommentRes;
import com.ikchi.annode.domain.dto.pospace.PospaceInfoRes;
import com.ikchi.annode.domain.dto.pospace.PospaceReq;
import com.ikchi.annode.domain.dto.pospace.PospaceUpdate;
import com.ikchi.annode.domain.dto.user.FollowRes;
import com.ikchi.annode.domain.entity.Pospace;
import com.ikchi.annode.domain.entity.PospaceComment;
import com.ikchi.annode.domain.entity.ReportUserAndPospace;
import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.domain.entity.UserRelationShip;
import com.ikchi.annode.repository.PospaceRepository;
import com.ikchi.annode.repository.ReportUserRepository;
import com.ikchi.annode.repository.UserRepository;
import com.ikchi.annode.security.AESUtil;
import jakarta.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PospaceService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PospaceRepository pospaceRepository;
    private final ReportUserRepository reportUserRepository;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;

    @Value("${client.url}")
    private String clientUrl;

    @Transactional
    public void createPospace(PospaceReq pospaceReq, String userMail) {

        User user = userService.findUserByMail(userMail);
        List<String> pospaceImgFileUrlList = null;

        if ((pospaceReq.getPospaceContent() == null || pospaceReq.getPospaceContent().equals(""))
            && (pospaceReq.getProfileImageFiles() == null || pospaceReq.getProfileImageFiles()
            .isEmpty())) {
            throw new IllegalStateException(
                PospaceExceptionMessage.POST_OR_IMAGE_REQUIRED.getMessage());
        }

        if (pospaceReq.getProfileImageFiles() != null && !pospaceReq.getProfileImageFiles()
            .isEmpty()) {
            pospaceImgFileUrlList = fileUploadService.storeFiles(
                pospaceReq.getProfileImageFiles(),
                user.getUserIdentifier());
        }

        Visibility visibility = Visibility.fromString(pospaceReq.getVisibility())
            .orElseThrow(() -> new NoSuchElementException(Visibility.NO_SUCH_VISIBILITY_MSG));

        Pospace pospace = new Pospace(user, pospaceReq.getPospaceContent(),
            pospaceReq.getMaxAnnode(), visibility,
            pospaceReq.getTolkOpen(), pospaceImgFileUrlList);

        if (!pospaceReq.getUserTagList().isEmpty()) {
            pospace.setPospaceUserTagList(pospaceReq.getUserTagList());
        }

        pospaceRepository.savePospace(pospace);

        List<String> pospaceUserTagList = pospace.getPospaceUserTagList();

        if (pospaceUserTagList != null && !pospaceUserTagList.isEmpty()) {
            List<String> userFmcTokenList = userRepository.findUserFmcTokenList(pospaceUserTagList);

            if (!userFmcTokenList.isEmpty()) {
                String messageUrl = clientUrl + "/space/pospace" + pospace.getId();
                for (String fcmToken : userFmcTokenList) {

                    notificationService.sendNotification(fcmToken,
                        user.getNickName() + "님이 작성하신 게시물에 Tag 되셨습니다", messageUrl);
                }
            }
        }


    }

    @Transactional
    public void updatePospace(PospaceUpdate pospaceUpdate, String email) {

        // 게시글 수정권한 있는지 확인
        Pospace pospace = getPospaceById(pospaceUpdate.getPospaceId());

        User user = userService.findUserByMail(email);

        if (!pospace.getWriter().equals(user)) {
            throw new IllegalStateException(
                PospaceExceptionMessage.NO_EDIT_PERMISSION.getMessage());
        }

        if (pospaceUpdate.getMaxAnnode() != null) {
            pospace.setMaxAnnode(pospaceUpdate.getMaxAnnode());
        }

        if (pospaceUpdate.getPospaceContent() != null) {
            pospace.setPospaceContent(pospaceUpdate.getPospaceContent());
        }

        if (pospaceUpdate.getVisibility() != null) {
            Visibility updateVisibility = Visibility.valueOf(
                pospaceUpdate.getVisibility().toUpperCase());
            pospace.setVisibility(updateVisibility);
        }

        pospace.setTolkOpen(pospaceUpdate.getTolkOpen());

        // 이미지 제거 반영
        List<String> deleteFileImgList = pospaceUpdate.getDeleteFileImgList();
        List<String> pospaceImgFileUrlList = pospace.getPospaceImgFileUrlList();

        if (!deleteFileImgList.isEmpty()) {
            fileUploadService.deleteImgFile(deleteFileImgList);

            List<String> newPospaceImgUrlList = pospaceImgFileUrlList.stream()
                .filter(url -> !deleteFileImgList.contains(url)).collect(
                    Collectors.toList());

            pospaceImgFileUrlList = newPospaceImgUrlList;

        }

        pospace.setPospaceImgFileUrlList(pospaceImgFileUrlList);


    }


    @Transactional
    public void deletePospace(Long pospaceId, String email) {
        User user = userService.findUserByMail(email);

        Pospace pospace = getPospaceById(pospaceId);

        if (pospace.getWriter().equals(user)) {
            fileUploadService.deleteImgFile(pospace.getPospaceImgFileUrlList());
            pospaceRepository.deletePospace(pospace);
        }
    }

    // version을 사용하여 동시성을 지키며 정상 수행될떄까지 좋아요하기


    @Transactional
    public PospaceInfoRes getPospaceInfo(Long pospaceId, String email) {

        Pospace pospace = getPospaceById(pospaceId);

        User writer = pospace.getWriter();

        List<String> pospaceUserTagList = pospace.getPospaceUserTagList();

        List<User> userTagList = pospaceUserTagList.stream()
            .map(userRepository::findUserByIdentifier)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

//        게시글 반환 DTO를 만들어서 개인정보에 안전한 게시글 응답 객체를 만듬
        PospaceInfoRes pospaceInfoRes = PospaceInfoRes.fromPospaceAndUser(pospace, writer,
            userTagList);

        // 로그인을 하지않았지만 공개상태의 컨텐츠인경우 응답함
        if (email == null) {
            if (pospace.getVisibility().equals(Visibility.ALL)) {
                return pospaceInfoRes;
            } else {
                throw new IllegalArgumentException(
                    PospaceExceptionMessage.LOGIN_REQUIRED_FOR_POST.getMessage());
            }
        }

        User user = userService.findUserByMail(email);

        String writerIdentifier = pospaceInfoRes.getWriterSimpleRes().getUserIdentifier();

        //  Pospace 작성자이면 수정권한을 추가해주고 즉시 응답
        if (writerIdentifier.equals(user.getUserIdentifier())) {
            pospaceInfoRes.setEditAuthority(true);
            return pospaceInfoRes;
        }

        if (pospace.getVisibility().equals(Visibility.ALL)) {
            return pospaceInfoRes;
        }

        List<String> followIdentifierList = userService.getFollowList(email).stream()
            .map(FollowRes::getFollowIdentifier).collect(
                Collectors.toList());

        if (pospace.getVisibility().equals(Visibility.FOLLOWER)) {

            if (!followIdentifierList.contains(writer.getUserIdentifier())) {
                throw new RuntimeException(
                    PospaceExceptionMessage.NO_FOLLOW_RELATIONSHIP.getMessage());
            }

        }

        if (pospace.getVisibility().equals(Visibility.CROSSFOLLOW)) {

            List<String> writerFolloewIdentifierList = userService.getFollowList(
                    writer.getEmail()).stream()
                .map(FollowRes::getFollowIdentifier).collect(
                    Collectors.toList());

            boolean isCrossFollow = followIdentifierList.contains(writer.getUserIdentifier())
                && writerFolloewIdentifierList.contains(user.getUserIdentifier());
            if (!isCrossFollow) {
                throw new IllegalStateException(
                    PospaceExceptionMessage.NO_CROSSFOLLOW_RELATIONSHIP.getMessage());
            }

        }

        return pospaceInfoRes;
    }


    @Transactional
    public Page<PagePospaceRes> getList(Pageable pageable, Long followPospaceIdx,
        Long publicPospaceIdx, Long crossFollowPospaceIdx, String email, Boolean isFirstReq) {

        int pageSize = pageable.getPageSize();

        // 자신이 Follow 하고있는 유저식별자 목록
        List<String> followIdentifierList = userService.getFollowList(email).stream()
            .map(FollowRes::getFollowIdentifier).collect(
                Collectors.toList());

        // 자신의 Follower들 목록
        List<UserRelationShip> followerRelationShipList = userService.getFollowerRelationShipList(
            email);

        // 자신의 Follower들 유저식별자 목록
        List<String> followerIdentifierList = followerRelationShipList.stream()
            .map(UserRelationShip::getUser)
            .map(User::getUserIdentifier)
            .collect(Collectors.toList());

        Page<PagePospaceRes> followPagePospaceRes = pospaceRepository.findRoomsBySearch(
            isFirstReq,
            followIdentifierList, followPospaceIdx, pageable);

        int followPospaceSize = followPagePospaceRes.getNumberOfElements();

        Page<PagePospaceRes> publicPagePospaceRes = pospaceRepository.findRoomsBySearch2(
            isFirstReq,
            followIdentifierList,
            publicPospaceIdx,
            pageable);

        User user = userService.findUserByMail(email);

        Page<PagePospaceRes> crossFollowPagePospaceRes = pospaceRepository.findRoomsBySearch3(
            isFirstReq,
            followIdentifierList,
            followerIdentifierList,
            crossFollowPospaceIdx,
            pageable,
            user
        );

        // pospace를 교차로 섞어서 통합해 반환한다
        List<PagePospaceRes> combinedList = new ArrayList<>();
        List<PagePospaceRes> followContentList = followPagePospaceRes.getContent();
        List<PagePospaceRes> publicContentList = publicPagePospaceRes.getContent();
        List<PagePospaceRes> crossContentList = crossFollowPagePospaceRes.getContent();

        int maxIndex = Math.max(Math.max(followContentList.size(), publicContentList.size()),
            crossContentList.size());

        for (int i = 0; i < maxIndex; i++) {
            if (i < followContentList.size()) {
                combinedList.add(followContentList.get(i));
            }
            if (i < publicContentList.size()) {
                combinedList.add(publicContentList.get(i));
            }
            if (i < crossContentList.size()) {
                combinedList.add(crossContentList.get(i));
            }
        }

        // Create a new page using the combined content
        Page<PagePospaceRes> combinedPage = new PageImpl<>(combinedList, pageable,
            combinedList.size());

        return combinedPage;
    }

    @Transactional
    public Page<PagePospaceRes> getUserPospaceList(Pageable pageable, Long userPospaceIdx,
        String userIdentifier, String email, Boolean isFirstReq) {

        // 계정이 없으면 글은 접근금지

        User targetUser = userService.findUserByIdentifier(userIdentifier);

        User user = userService.findUserByMail(email);

        boolean followUserCheck1 = userService.getFollowUserCheck(user, targetUser);
        boolean followUserCheck2 = userService.getFollowUserCheck(targetUser, user);

        if ((followUserCheck1 && followUserCheck2) || targetUser.equals(user)) {
            // 모든 게시물을 조회할수있는 crossfollow case
            Page<PagePospaceRes> roomsBySearchAll = pospaceRepository.findRoomsBySearchAll(
                userPospaceIdx, pageable, targetUser, isFirstReq);

            return roomsBySearchAll;

        } else if (followUserCheck1 && !followUserCheck2) {
            // 일반 follow case

            Page<PagePospaceRes> roomsBySearchFollow = pospaceRepository.findRoomsBySearchFollow(
                userPospaceIdx, pageable, userIdentifier, isFirstReq);

            return roomsBySearchFollow;

        } else {
            // 누구도 follow 하지않음
        }

        return null;
    }

    @Transactional
    public List<PospaceCommentRes> createPospaceComment(
        PospaceCommentCreateReq pospaceCommentCreateReq,
        String email) {

        Long pospaceId = pospaceCommentCreateReq.getPospaceId();

        Pospace pospace = getPospaceById(pospaceId);

        User user = userService.findUserByMail(email);
        PospaceComment pospaceComment = new PospaceComment(pospace,
            user, pospaceCommentCreateReq.getPospaceCommentContent());

        pospaceRepository.savePospaceComment(pospaceComment);

        if (!pospace.getWriter().equals(user)) {

            String messageUrl = clientUrl + "/space/pospace" + pospace.getId();

            notificationService.sendNotification(pospace.getWriter().getFcmToken(),
                "작성하신 게시물에 " + user.getNickName() + "님 으로부터 댓글이 도착했습니다",
                messageUrl);


        }

        List<PospaceCommentRes> pospaceCommentResList = pospace.getPospaceCommentList().stream()
            .map(PospaceCommentRes::fromPospaceComment)
            .collect(Collectors.toList());

        return pospaceCommentResList;
    }

    @Transactional
    public List<PospaceCommentRes> removePospaceComment(Long commentId, String email) {

        User user = userService.findUserByMail(email);

        PospaceComment comment = pospaceRepository.findComment(commentId)
            .orElseThrow(() -> new NoSuchElementException(
                PospaceExceptionMessage.NO_SUCH_COMMENT.getMessage()));

        if (!comment.getCommentWriter().equals(user)) {
            throw new IllegalStateException(
                PospaceExceptionMessage.NOT_COMMENT_WRITER.getMessage());
        }

        pospaceRepository.deleteComment(comment);

        Pospace pospace = comment.getPospace();

        List<PospaceCommentRes> pospaceCommentResList = pospace.getPospaceCommentList().stream()
            .filter(pospaceComment -> !pospaceComment.getId().equals(commentId))
            .map(PospaceCommentRes::fromPospaceComment)
            .collect(Collectors.toList());

        return pospaceCommentResList;
    }

    @Transactional
    public List<PospaceCommentRes> findPospaceCommentList(Long pospaceId) {

        List<PospaceComment> pospaceCommentList = pospaceRepository.findPospaceCommentList(
            pospaceId);

        List<PospaceCommentRes> pospaceCommentResList = pospaceCommentList.stream()
            .map(PospaceCommentRes::fromPospaceComment)
            .collect(Collectors.toList());

        return pospaceCommentResList;

    }


    @Transactional
    public Map<String, String> pospaceAccess(Long pospaceId, String userMail) {

        User user = userRepository.findUserByMailWithLock(userMail)
            .orElseThrow(() -> new NoSuchElementException(
                UserExceptionMessage.NON_EXISTENT_USER.getMessage()));

        Pospace pospace = getPospaceById(pospaceId);

        if (isUserAlreadyInRoom(user)) {
            throw new IllegalStateException(PospaceExceptionMessage.ALREADY_IN_ROOM.getMessage());
        }

        // 유효성 검증 통과시 암호화된 pospaceToken을 반환
        if (!pospace.getWriter().equals(user)) {

            validateRoomEntry(pospace, user);
        }

        // 유효성 검사를 한뒤 접속하려는 유저의 email을 사용하여 암호화를 한다
        // 이후 암호화된 roomToken을 클라이언트에게 응답한다 참고로 복호화를 할때도 email을 대칭키로 사용한다

        String encryptedPospaceToken = AESUtil.encrypt(pospace.getPospaceToken(), user.getEmail());

        Map<String, String> myPospaceAccessMap = new HashMap<>();

        myPospaceAccessMap.put(user.getUserIdentifier(), encryptedPospaceToken);

        return myPospaceAccessMap;
    }

    // pospace 입장시 유효성 검사를 진행하고 성공시 대화에 참여할수있는 암호화된 pospaceToken을 반환
    public void validateRoomEntry(Pospace pospace, User user) {

        String writerIdentifier = pospace.getWriter().getUserIdentifier();

        boolean isfollowCheck = userService.getFollowList(user.getEmail()).stream()
            .map(FollowRes::getFollowIdentifier)
            .anyMatch(item ->
                item == writerIdentifier
            );

        Visibility visibility = pospace.getVisibility();

        User writer = pospace.getWriter();

        switch (visibility) {
            case ALL -> {
            }
            case FOLLOWER -> {
                if (!isTargetUserFollow(user, writer)) {
                    throw new IllegalStateException(
                        PospaceExceptionMessage.NO_FOLLOW_RELATIONSHIP.getMessage());
                }
            }
            case CROSSFOLLOW -> {
                if (!isTargetUserCrossFollow(user, writer)) {
                    throw new IllegalStateException(
                        PospaceExceptionMessage.NO_CROSSFOLLOW_RELATIONSHIP.getMessage());
                }
            }
            default -> throw new IllegalStateException(
                PospaceExceptionMessage.UNEXPECTED_ERROR.getMessage());
        }

    }

    // 방에 입장중인 유저인지 확인
    public boolean isUserAlreadyInRoom(User user) {

        if (user.getJoinedRoom() != null) {
            return true;
        }

        return false;
    }


    public boolean isTargetUserFollow(User user, User targetUser) {

        List<FollowRes> followList = userService.getFollowList(user.getEmail());

        boolean isTargetUserFollow = followList.stream()
            .anyMatch(item -> item.getFollowIdentifier().equals(targetUser.getUserIdentifier()));

        return isTargetUserFollow ? true : false;
    }

    public boolean isTargetUserCrossFollow(User user, User targetUser) {

        List<UserRelationShip> crossFollowsByUser = userService.getCrossFollowsByUser(
            user.getEmail());

        if (crossFollowsByUser.isEmpty()) {
            throw new IllegalStateException(
                PospaceExceptionMessage.NO_CROSSFOLLOW_USERS.getMessage());
        }

        boolean isTargetUserCrossFollow = crossFollowsByUser.stream()
            .anyMatch(item -> item.getFollowingUser().getUserIdentifier()
                .equals(targetUser.getUserIdentifier()));

        return isTargetUserCrossFollow ? true : false;
    }

    // 낙관적락과 Version을 사용하여 좋아요기능을 수행
    // 영원한 좋아요 로직 호출을 방지하기위해 무한루프의 최대횟수를 방지하기
    @Transactional
    public Integer pospaceIncreaseLike(Long pospaceId, String email) {

        User user = userService.findUserByMail(email);
        String userIdentifier = user.getUserIdentifier();

        Integer likeCount = null;

        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            try {

                Pospace pospace = getPospaceById(pospaceId);

                List<String> pospaceLikeUserIdentifiers = pospace.getPospaceLikeUsers();

                if (pospaceLikeUserIdentifiers.contains(userIdentifier)) {
                    throw new IllegalStateException(
                        PospaceExceptionMessage.ALREADY_LIKED_POST.getMessage());
                }

                pospaceLikeUserIdentifiers.add(user.getUserIdentifier());

                if (!pospace.getWriter().equals(user)) {

                    String messageUrl = clientUrl + "/space/pospace" + pospace.getId();

                    notificationService.sendNotification(pospace.getWriter().getFcmToken(),
                        "작성 게시물이 " + user.getNickName() + "님에게 좋아요를 받았습니다!", messageUrl);

                }

                likeCount = pospaceLikeUserIdentifiers.size();

                return likeCount;


            } catch (OptimisticLockException e) {
                if (i == maxAttempts - 1) {
                    throw new IllegalStateException(
                        PospaceExceptionMessage.UNEXPECTED_ERROR.getMessage());
                }
            }
        }
        return likeCount;
    }


    @Transactional
    public void reportPospace(ReportReq reportReq, String email) {

        User user = userService.findUserByMail(email);

        Pospace pospace = getPospaceById(reportReq.getPospaceId());

        ReportUserAndPospace reportUserAndPospace = new ReportUserAndPospace(user,
            reportReq.getReportReason(), pospace);

        reportUserRepository.save(reportUserAndPospace);
    }


    private Pospace getPospaceById(Long pospaceId) {
        return pospaceRepository.findOne(pospaceId)
            .orElseThrow(
                () -> new NoSuchElementException(
                    PospaceExceptionMessage.NO_SUCH_POST.getMessage()));
    }

}
