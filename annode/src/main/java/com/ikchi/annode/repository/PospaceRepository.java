package com.ikchi.annode.repository;

import com.ikchi.annode.domain.dto.pospace.PagePospaceRes;
import com.ikchi.annode.domain.entity.Pospace;
import com.ikchi.annode.domain.entity.PospaceComment;
import com.ikchi.annode.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class PospaceRepository {

    @PersistenceContext
    private EntityManager em;


    public void savePospace(Pospace pospace) {
        em.persist(pospace);
    }


    public void savePospaceComment(PospaceComment pospaceComment) {
        em.persist(pospaceComment);
    }

    @Transactional
    public void deletePospace(Pospace pospace) {
        em.remove(pospace);
    }

    @Transactional
    public Optional<Pospace> findByWriter(User user) {
        try {
            Pospace pospace = em.createQuery("SELECT p FROM Pospace p WHERE p.writer = :user",
                    Pospace.class)
                .setParameter("user", user)
                .getSingleResult();
            return Optional.ofNullable(pospace);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    @Transactional
    public void removeUserTagFromPospace(User user) {

        List<Pospace> pospaceList = em.createQuery(
                "SELECT p FROM Pospace p JOIN p.pospaceUserTagList t WHERE t = :userIdentifier",
                Pospace.class)
            .setParameter("userIdentifier", user.getUserIdentifier())
            .getResultList();

        pospaceList.forEach(
            pospace -> pospace.getPospaceUserTagList().remove(user.getUserIdentifier()));

    }

    @Transactional
    public Optional<PospaceComment> findComment(Long commentId) {
        try {

            PospaceComment pospaceComment = em.find(PospaceComment.class, commentId);

            return Optional.ofNullable(pospaceComment);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteComment(PospaceComment comment) {
        em.remove(comment);
    }


    @Transactional
    public Optional<Pospace> findOne(Long pospaceId) {
        try {
            Pospace pospace = em.find(Pospace.class, pospaceId);
            return Optional.ofNullable(pospace);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Pospace> findOneWithReadLock(Long pospaceId) {
        try {
            Pospace pospace = em.find(Pospace.class, pospaceId, LockModeType.PESSIMISTIC_READ);
            return Optional.ofNullable(pospace);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Pospace> findByToken(String pospaceToken) {
        try {
            Pospace pospace = em.createQuery(
                    "SELECT p FROM Pospace p WHERE p.pospaceToken = :pospaceToken",
                    Pospace.class)
                .setParameter("pospaceToken", pospaceToken)
                .getSingleResult();
            return Optional.ofNullable(pospace);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    public Page<PagePospaceRes> findRoomsBySearch(Boolean isFristReq,
        List<String> followIdentifierList,
        Long followPospaceIdx, Pageable pageable) {

        if (followIdentifierList.isEmpty() || followPospaceIdx == 0) {
            return Page.empty();
        }

        Sort.Direction sortDirection = pageable.getSort().getOrderFor("ps.id").getDirection();

        String whereQuery = followPospaceIdx == 1 && isFristReq
            ? "WHERE wr.userIdentifier IN :followList AND ps.visibility = 'FOLLOWER' AND ps.id >= :cursor "
            : "WHERE wr.userIdentifier IN :followList AND ps.visibility = 'FOLLOWER' AND ps.id <= :cursor ";

        List<PagePospaceRes> resultList = em.createQuery(
                "SELECT new com.ikchi.annode.domain.dto.pospace.PagePospaceRes(ps.id, size(ps.pospaceLikeUsers), ps.pospaceContent, ps.createdTime, wr.userIdentifier, wr.nickName, wr.profileImgFileUrl, ps.visibility ) "
                    + "FROM Pospace ps JOIN ps.writer wr "
                    + whereQuery
                    + "ORDER BY ps.id " + sortDirection,
                PagePospaceRes.class)
            .setParameter("cursor", followPospaceIdx)
            .setParameter("followList", followIdentifierList)
            .setMaxResults(4)
            .getResultList();

        for (PagePospaceRes res : resultList) {
            Pospace pospace = em.find(Pospace.class, res.getPospaceId());
            res.setPospaceImgFileUrlList(pospace.getPospaceImgFileUrlList());
        }

        return new PageImpl<>(resultList, pageable, resultList.size());
    }


    public Page<PagePospaceRes> findRoomsBySearch2(Boolean isFristReq,
        List<String> followIdentifierList,
        Long publicPospaceIdx, Pageable pageable) {

        if (publicPospaceIdx == 0) {
            return Page.empty();
        }

        String whereQuery = publicPospaceIdx == 1 && isFristReq
            ? "WHERE ps.visibility = 'ALL' AND ps.id >= :cursor "
            : "WHERE ps.visibility = 'ALL' AND ps.id <= :cursor ";

        Sort.Direction sortDirection = pageable.getSort().getOrderFor("ps.id").getDirection();

        List<PagePospaceRes> resultList = em.createQuery(
                "SELECT new com.ikchi.annode.domain.dto.pospace.PagePospaceRes(ps.id, size(ps.pospaceLikeUsers), ps.pospaceContent , ps.createdTime, wr.userIdentifier, wr.nickName, wr.profileImgFileUrl, ps.visibility ) "
                    + "FROM Pospace ps JOIN ps.writer wr "
                    + whereQuery
                    + "ORDER BY ps.id " + sortDirection,
                PagePospaceRes.class)
            .setParameter("cursor", publicPospaceIdx)
            .setMaxResults(3)
            .getResultList();

        for (PagePospaceRes res : resultList) {
            Pospace pospace = em.find(Pospace.class, res.getPospaceId());
            res.setPospaceImgFileUrlList(pospace.getPospaceImgFileUrlList());
        }

        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    public Page<PagePospaceRes> findRoomsBySearch3(Boolean isFristReq,
        List<String> followIdentifierList,
        List<String> followerIdentifierList,
        Long crossFollowPospaceIdx, Pageable pageable, User user) {

        if (crossFollowPospaceIdx == 0 || followIdentifierList.isEmpty()
            || followerIdentifierList.isEmpty()) {
            return Page.empty();
        }

        String whereQuery = crossFollowPospaceIdx == 1 && isFristReq
            ? "WHERE wr.userIdentifier IN :followList AND wr.userIdentifier IN :followerList AND ps.visibility = 'CROSSFOLLOW' AND ps.id >= :cursor "
            : "WHERE wr.userIdentifier IN :followList AND wr.userIdentifier IN :followerList AND ps.visibility = 'CROSSFOLLOW' AND ps.id <= :cursor ";

        Sort.Direction sortDirection = pageable.getSort().getOrderFor("ps.id").getDirection();

        Query query = em.createQuery(
                "SELECT new com.ikchi.annode.domain.dto.pospace.PagePospaceRes(ps.id, size(ps.pospaceLikeUsers), ps.pospaceContent , ps.createdTime, wr.userIdentifier, wr.nickName, wr.profileImgFileUrl, ps.visibility ) "
                    + "FROM Pospace ps JOIN ps.writer wr "
                    + whereQuery
                    + "ORDER BY ps.id " + sortDirection,
                PagePospaceRes.class)
            .setParameter("cursor", crossFollowPospaceIdx)
            .setParameter("followList", followIdentifierList)
            .setParameter("followerList", followerIdentifierList)
            .setMaxResults(3);

        if (!followIdentifierList.isEmpty()) {
            query = query.setParameter("followList", followIdentifierList);
        }

        List<PagePospaceRes> resultList = query.getResultList();

        for (PagePospaceRes res : resultList) {
            Pospace pospace = em.find(Pospace.class, res.getPospaceId());
            res.setPospaceImgFileUrlList(pospace.getPospaceImgFileUrlList());
        }

        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    public Page<PagePospaceRes> findRoomsBySearchAll(Long userPspaceIdx, Pageable pageable,
        User targetUser, Boolean isFirstReq) {

        if (userPspaceIdx == 0) {
            return Page.empty();
        }

        Sort.Direction sortDirection = pageable.getSort().getOrderFor("ps.id").getDirection();

        String whereQuery = userPspaceIdx == 1 && isFirstReq
            ? "WHERE wr.userIdentifier = :targetUser AND ps.id >= :cursor "
            : "WHERE wr.userIdentifier = :targetUser AND ps.id <= :cursor ";

        Query query = em.createQuery(
                "SELECT new com.ikchi.annode.domain.dto.pospace.PagePospaceRes(ps.id, size(ps.pospaceLikeUsers), ps.pospaceContent, ps.createdTime, wr.userIdentifier, wr.nickName, wr.profileImgFileUrl, ps.visibility ) "
                    + "FROM Pospace ps JOIN ps.writer wr "
                    + whereQuery
                    + "ORDER BY ps.id " + sortDirection,
                PagePospaceRes.class)
            .setParameter("cursor", userPspaceIdx)
            .setParameter("targetUser", targetUser.getUserIdentifier())
            .setMaxResults(11);

        List<PagePospaceRes> resultList = query.getResultList();

        for (PagePospaceRes res : resultList) {
            Pospace pospace = em.find(Pospace.class, res.getPospaceId());
            res.setPospaceImgFileUrlList(pospace.getPospaceImgFileUrlList());
        }

        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    public Page<PagePospaceRes> findRoomsBySearchFollow(Long userPspaceIdx, Pageable pageable,
        String userIdentifier, Boolean isFirstReq) {

        if (userPspaceIdx == 0) {
            return Page.empty();
        }

        String whereQuery = userPspaceIdx == 1 && isFirstReq
            ? "WHERE wr.userIdentifier = :targetUserIdentifier AND ps.id >= :cursor AND ( ps.visibility = 'ALL' OR ps.visibility = 'FOLLOWER' ) "
            : "WHERE wr.userIdentifier = :targetUserIdentifier AND ps.id <= :cursor AND ( ps.visibility = 'ALL' OR ps.visibility = 'FOLLOWER' ) ";

        Sort.Direction sortDirection = pageable.getSort().getOrderFor("ps.id").getDirection();

        Query query = em.createQuery(
                "SELECT new com.ikchi.annode.domain.dto.pospace.PagePospaceRes(ps.id, size(ps.pospaceLikeUsers), ps.pospaceContent , ps.createdTime, wr.userIdentifier, wr.nickName, wr.profileImgFileUrl, ps.visibility ) "
                    + "FROM Pospace ps JOIN ps.writer wr "
                    + whereQuery
                    + "ORDER BY ps.id " + sortDirection,
                PagePospaceRes.class)
            .setParameter("cursor", userPspaceIdx)
            .setParameter("targetUserIdentifier", userIdentifier)
            .setMaxResults(11);

        List<PagePospaceRes> resultList = query.getResultList();

        for (PagePospaceRes res : resultList) {
            Pospace pospace = em.find(Pospace.class, res.getPospaceId());
            res.setPospaceImgFileUrlList(pospace.getPospaceImgFileUrlList());
        }

        return new PageImpl<>(resultList, pageable, resultList.size());
    }


    public List<PospaceComment> findPospaceCommentList(Long pospaceId) {

        List<PospaceComment> pospaceCommentList = em.createQuery(
                "SELECT pc FROM PospaceComment pc WHERE pc.pospace.id = :pospaceId",
                PospaceComment.class)
            .setParameter("pospaceId", pospaceId)
            .getResultList();

        return pospaceCommentList;

    }


}
