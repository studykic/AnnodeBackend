package com.ikchi.annode.controller;

import com.ikchi.annode.annotation.JwtToUser;
import com.ikchi.annode.domain.dto.ReportReq;
import com.ikchi.annode.domain.dto.pospace.PagePospaceRes;
import com.ikchi.annode.domain.dto.pospace.PospaceCommentCreateReq;
import com.ikchi.annode.domain.dto.pospace.PospaceCommentRes;
import com.ikchi.annode.domain.dto.pospace.PospaceInfoRes;
import com.ikchi.annode.domain.dto.pospace.PospaceReq;
import com.ikchi.annode.domain.dto.pospace.PospaceUpdate;
import com.ikchi.annode.service.PospaceService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PospaceController {


    @Autowired
    private final PospaceService pospaceService;

    @PostMapping("/pospace/create")
    public void createRoom(
        @RequestParam(name = "pospaceContent", required = false) String pospaceContent,
        @RequestParam(name = "maxAnnode", required = false) Integer maxAnnode,
        @RequestParam(name = "visibility", required = false) String visibility,
        @RequestParam(name = "userTagList", required = false) List<String> userTagList,
        @RequestParam(name = "tolkOpen", required = false) Boolean tolkOpen,
        @RequestParam(name = "profileImageFiles", required = false) List<MultipartFile> profileImageFiles,
        @JwtToUser String email) {

        PospaceReq pospaceReq = new PospaceReq(pospaceContent, maxAnnode, visibility,
            userTagList, tolkOpen, profileImageFiles);

        pospaceService.createPospace(pospaceReq, email);

    }

    @PatchMapping("/pospace/update")
    public void createRoom(@RequestBody PospaceUpdate pospaceUpdate,
        @JwtToUser String email) {

        pospaceService.updatePospace(pospaceUpdate, email);

    }

    @DeleteMapping("/pospace/delete")
    public void deletePospace(
        @RequestParam Long pospaceId,
        @JwtToUser String email) {

        pospaceService.deletePospace(pospaceId, email);

    }

    // Pospace 접근시 Pospace 최대인원수 , 대화방이미접속유무를 검증한뒤 입장이 허용되면 요청한 유저의 이메일로 인코딩 처리된 pospaceToken을 반환한다 ( pospaceToken 탈취방지 )
    // 이후 클라이언트는 pospaceToken을 반환받으면 websocket을 통해 시그널링 서버에 afterConnectionEstablished 요청을 한다
    @GetMapping("/pospace/access")
    @Transactional
    public Map<String, String> pospaceAccess(@RequestParam Long pospaceId,
        @JwtToUser String userMail) {
        Map<String, String> myPospaceAccessMap = pospaceService.pospaceAccess(pospaceId, userMail);

        return myPospaceAccessMap;
    }

    @GetMapping("/pospace/list")
    public Page<PagePospaceRes> getPagePospaceResList(
        @JwtToUser String email,
        @RequestParam(defaultValue = "1") Long followPospaceIdx,
        @RequestParam(defaultValue = "1") Long publicPospaceIdx,
        @RequestParam(defaultValue = "1") Long crossFollowPospaceIdx,
        @RequestParam Boolean isFirstReq,
        @PageableDefault(size = 10, sort = "ps.id", direction = Direction.DESC) Pageable pageable) {

        Page<PagePospaceRes> PageInfoReslist = pospaceService.getList(pageable, followPospaceIdx,
            publicPospaceIdx,
            crossFollowPospaceIdx,
            email, isFirstReq);

        return PageInfoReslist;
    }

    @GetMapping("/user/info/pospace/list")
    public Page<PagePospaceRes> getUserPagePospaceResList(
        @JwtToUser String email,
        @RequestParam String userIdentifier,
        @RequestParam(defaultValue = "1") Long userPospaceIdx,
        @RequestParam Boolean isFirstReq,
        @PageableDefault(size = 10, sort = "ps.id", direction = Direction.DESC) Pageable pageable) {

        Page<PagePospaceRes> PageInfoReslist = pospaceService.getUserPospaceList(pageable,
            userPospaceIdx,
            userIdentifier,
            email, isFirstReq);

        return PageInfoReslist;
    }


    // 상세한 게시글보여주기
    @GetMapping("/view/pospace/info")
    public PospaceInfoRes getPospaceInfo(@JwtToUser String email, @RequestParam Long pospaceId) {

        PospaceInfoRes pospaceInfoRes = pospaceService.getPospaceInfo(pospaceId, email);

        return pospaceInfoRes;
    }


    @GetMapping("/pospace/comment/list")
    public List<PospaceCommentRes> findPospaceCommentList(
        @RequestParam Long pospaceId) {

        System.out.println("pospaceId = " + pospaceId);
        List<PospaceCommentRes> pospaceCommentResList = pospaceService.findPospaceCommentList(
            pospaceId);

        return pospaceCommentResList;
    }

    @PostMapping("/pospace/comment/create")
    public List<PospaceCommentRes> commentCreate(
        @RequestBody PospaceCommentCreateReq pospaceCommentCreateReq,
        @JwtToUser String email) {
        List<PospaceCommentRes> pospaceCommentResList = pospaceService.createPospaceComment(
            pospaceCommentCreateReq, email);

        return pospaceCommentResList;
    }

    @DeleteMapping("/pospace/comment/delete")
    public List<PospaceCommentRes> commentDelete(@RequestParam Long commentId,
        @JwtToUser String email) {

        List<PospaceCommentRes> pospaceCommentResList = pospaceService.removePospaceComment(
            commentId, email);

        return pospaceCommentResList;

    }

    @PostMapping("/pospace/like")
    public Map<Long, Integer> createRoom(@RequestParam Long pospaceId,
        @JwtToUser String email) {

        Map<Long, Integer> aaa = new HashMap<>();

        Integer likeCount = pospaceService.pospaceIncreaseLike(pospaceId, email);

        aaa.put(pospaceId, likeCount);

        return aaa;
    }

    @PostMapping("/pospace/report")
    public void reportPospace(@RequestBody ReportReq reportReq,
        @JwtToUser String email) {

        pospaceService.reportPospace(reportReq, email);

    }
}


