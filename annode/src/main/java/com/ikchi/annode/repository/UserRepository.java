package com.ikchi.annode.repository;

import com.ikchi.annode.domain.entity.AnnodeFollow;
import com.ikchi.annode.domain.entity.BanInfo;
import com.ikchi.annode.domain.entity.Invitation;
import com.ikchi.annode.domain.entity.User;
import com.ikchi.annode.domain.entity.UserRelationShip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    @PersistenceContext
    private final EntityManager em;


    public void save(User user) {
        em.persist(user);
    }

    public void delete(User user) {
        em.remove(user);
    }


    public void saveUserRelationShip(UserRelationShip userRelationship) {
        em.persist(userRelationship);
    }

    public void deleteUserRelationShip(UserRelationShip userRelationship) {
        em.remove(userRelationship);
    }


    public void saveAnnodeFollow(AnnodeFollow annodeFollow) {
        em.persist(annodeFollow);
    }


    public void deleteAnnodeFollow(AnnodeFollow annodeFollow) {
        em.remove(annodeFollow);
    }


    public Optional<AnnodeFollow> findAnnodeFollow(Long annodeFollowId) {
        AnnodeFollow annodeFollow = em.find(AnnodeFollow.class, annodeFollowId);
        return Optional.ofNullable(annodeFollow);
    }


    public List<UserRelationShip> findUserRelationShipList(User user) {
        return em.createQuery("SELECT ur FROM UserRelationShip ur WHERE ur.user = :user",
                UserRelationShip.class)
            .setParameter("user", user)
            .getResultList();
    }

    public List<UserRelationShip> findFollowerRelationShipList(User targetUser) {
        return em.createQuery(
                "SELECT ur FROM UserRelationShip ur WHERE ur.followingUser = :targetUser",
                UserRelationShip.class)
            .setParameter("targetUser", targetUser)
            .getResultList();
    }

    public Optional<UserRelationShip> findUserRelationShip(User user, User targetUser) {
        TypedQuery<UserRelationShip> query = em.createQuery(
                "SELECT ur FROM UserRelationShip ur WHERE ur.user = :user AND ur.followingUser = :targetUser ",
                UserRelationShip.class)
            .setParameter("user", user)
            .setParameter("targetUser", targetUser);

        Optional<UserRelationShip> optionalUserRelationShip = query.getResultList().stream()
            .findFirst();
        return optionalUserRelationShip;
    }


    public List<AnnodeFollow> findAnnodeFollowByRequester(User requester) {
        TypedQuery<AnnodeFollow> query = em.createQuery(
            "SELECT af FROM AnnodeFollow af WHERE af.requester = :requester", AnnodeFollow.class);
        query.setParameter("requester", requester);
        return query.getResultList();
    }

    public List<AnnodeFollow> findAnnodeFollowByReceiver(User receiver) {
        TypedQuery<AnnodeFollow> query = em.createQuery(
            "SELECT af FROM AnnodeFollow af WHERE af.receiver = :receiver", AnnodeFollow.class);
        query.setParameter("receiver", receiver);
        return query.getResultList();
    }


    public Optional<User> findUserByIdentifier(String userIdentifier) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.userIdentifier = :userIdentifier",
            User.class);
        query.setParameter("userIdentifier", userIdentifier);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();
        return optionalUser;
    }

    public Optional<User> findUserByIdentifierOrEmail(String userSearchInput) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.userIdentifier = :userSearchInput OR u.email = :userSearchInput OR u.phoneNumber = :userSearchInput",
            User.class);
        query.setParameter("userSearchInput", userSearchInput);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();
        return optionalUser;
    }

    public List<String> findUserFmcTokenList(List<String> userIdentifierList) {
        List<String> userFmcTokenList = em.createQuery(
                "SELECT u.fcmToken FROM User u WHERE u.userIdentifier IN :userIdentifierList")
            .setParameter("userIdentifierList", userIdentifierList)
            .getResultList();

        return userFmcTokenList;
    }

    public List<User> findUserListByPhone(List<String> phoneNumbers) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.phoneNumber IN :phoneNumbers",
            User.class);
        query.setParameter("phoneNumbers", phoneNumbers);

        List<User> userList = query.getResultList();
        return userList;
    }


    // Mail을 통해 유저를 찾는다. email은 유니크한 값이기때문에 결과값은 1개를 목표로하는 쿼리 메소드
    public Optional<User> findUserByMail(String email) {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email",
            User.class);
        query.setParameter("email", email);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();
        return optionalUser;
    }

    public Optional<User> findUserByPhone(String phoneNumber) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber",
            User.class);
        query.setParameter("phoneNumber", phoneNumber);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();
        return optionalUser;
    }

    public Optional<User> findUserWithRelationshipsByEmail(String email) {
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u JOIN FETCH u.userRelationShipList WHERE u.email = :email",
            User.class);
        query.setParameter("email", email);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();
        return optionalUser;
    }


    // email을 통해 유저를 조회한후 좋아요를 1씩 증가시키는 쿼리 메소드
    // 또한 동시에 여러명이 좋아요를 누를수있기에 PESSIMISTIC_WRITE 락을 걸어 동시성 문제를 방지한다
    @Transactional
    public Optional<User> findUserByMailWithLock(String email) {
        // EntityManager를 사용하여 락을 설정하고 쿼리를 실행합니다.
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email",
            User.class);
        query.setParameter("email", email)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE);

        Optional<User> optionalUser = query.getResultList().stream().findFirst();

        return optionalUser;
    }

    @Transactional
    public Optional<Invitation> findInvitation(String invitationCode) {
        // EntityManager를 사용하여 락을 설정하고 쿼리를 실행합니다.
        TypedQuery<Invitation> query = em.createQuery(
            "SELECT iv FROM Invitation iv WHERE iv.invitationCode = :invitationCode",
            Invitation.class);

        query.setParameter("invitationCode", invitationCode);

        Optional<Invitation> optionalInvitation = query.getResultList().stream().findFirst();

        return optionalInvitation;
    }

    @Transactional
    public Optional<Invitation> findInvitationByinviteeUser(User inviteeUser) {
        // EntityManager를 사용하여 락을 설정하고 쿼리를 실행합니다.
        TypedQuery<Invitation> query = em.createQuery(
            "SELECT iv FROM Invitation iv WHERE iv.inviteeUser = :inviteeUser",
            Invitation.class);

        query.setParameter("inviteeUser", inviteeUser);

        Optional<Invitation> optionalInvitation = query.getResultList().stream().findFirst();

        return optionalInvitation;

    }

    @Transactional
    public void invitationsListSaveAll(List<Invitation> invitationsList) {
        for (Invitation invitation : invitationsList) {
            em.persist(invitation);
        }
    }

    public void banInfoAdd(BanInfo banInfo) {
        em.persist(banInfo);
    }

    public Boolean banUserCheck(String phoneNumber, String email) {
        TypedQuery<BanInfo> query = em.createQuery(
            "SELECT ban From BanInfo ban WHERE ban.phoneNumber = :phoneNumber OR ban.email = :email",
            BanInfo.class);

        query.setParameter("phoneNumber", phoneNumber);
        query.setParameter("email", email);

        List<BanInfo> userList = query.getResultList();
        if (userList.size() > 0) {
            return true;
        }
        return false;
    }


}
