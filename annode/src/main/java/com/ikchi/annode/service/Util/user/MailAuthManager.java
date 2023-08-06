//package com.ikchi.annode.service.Util;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import lombok.Getter;
//
//
//@Getter
//public class mailAuthManager {
//
//
//    private final Map<String, EmailAuthInfo> signUpMailAuthMap;
//    private final Map<String, EmailAuthInfo> pwResetMailAuthMap;
//
//    public mailAuthManager() {
//        this.signUpMailAuthMap = new HashMap<>();
//        this.pwResetMailAuthMap = new HashMap<>();
//    }
//
//    public Optional<EmailAuthInfo> getSignUpAuthInfo(String mail) {
//        EmailAuthInfo emailAuthInfo = signUpMailAuthMap.get(mail);
//        return Optional.ofNullable(emailAuthInfo);
//    }
//
//    public Optional<EmailAuthInfo> getPwResetAuthInfo(String mail) {
//        EmailAuthInfo emailAuthInfo = pwResetMailAuthMap.get(mail);
//        return Optional.ofNullable(emailAuthInfo);
//    }
//
//
//    public void putSignUpAuthInfo(String mail, EmailAuthInfo emailAuthInfo) {
//        signUpMailAuthMap.put(mail, emailAuthInfo);
//    }
//
//
//    public void putPwResetAuthInfo(String mail, EmailAuthInfo emailAuthInfo) {
//        pwResetMailAuthMap.put(mail, emailAuthInfo);
//    }
//
//    public void removeSignUpAuthInfo(String mail) {
//        signUpMailAuthMap.remove(mail);
//    }
//
//    public void removePwResetAuthInfo(String mail) {
//        pwResetMailAuthMap.remove(mail);
//    }
//}

package com.ikchi.annode.service.Util.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//특정 엔트리 갯수만큼 해시맵을 만든뒤 한계치가 찼으면 마지막 인증정보객체를 list에 저장한다
//그후 특정 주기마다 인증정보객체를 뒤에서 부터 순회하여 만료시간이 지난 객체를 찾고 그 객체의 index 이전에 생성된
//MapList의 요소들을 한번에 제거한다
// 장점 : 스케줄링을 이용하여 주기적인 검사를 하지않고 한계치가 찰때마다 마지막 엔트리만 만료시간 검사를 하여 한번에 삭제를할수있다
// 단점 : 만료시간이 지나도 메모리에 계속남아있을수있다 하지만 남아있더라도 미미한 사이즈만큼의 MAX_ENTRY를 설정하여 메모리를 효율적으로 사용할수있다
// 특이점 : 만약 인증번호를 연속으로 두번을 날렸는데 때마침 첫번째 인증정보객체가 저장된뒤 MapList의 한계치가 차서 새로운 MapList에 두번째 인증정보가 생성되었더라도
// getSignUpAuthInfo , removeSignUpAuthInfo 을 호출하여 인증정보를 가져올때 최근에 생긴 인증정보객체부터 가져오기때문에 유효성검사에도 문제가 없다

@Getter
@NoArgsConstructor
@Component
public class MailAuthManager {

    @Value("${mailauth.maxentry}")
    private int MAX_ENTRY;

    private final List<ConcurrentHashMap<String, EmailAuthInfo>> signUpMailAuthMapList = new ArrayList<>(
        Arrays.asList(new ConcurrentHashMap<>()));


    private final List<EmailAuthInfo> signUpMailExpiryCheckList = new ArrayList<>();


    private final List<ConcurrentHashMap<String, EmailAuthInfo>> pwResetMailAuthMapList = new ArrayList<>(
        Arrays.asList(new ConcurrentHashMap<>()));


    private final List<EmailAuthInfo> pwResetMailExpiryCheckList = new ArrayList<>();

    // singUpMailAuthMapList에 맵 추가한다 이때 만개단위의 엔트리가 채워지면 새롭게 맵을 추가시키기

    // 인증정보객체를 메일로 넣는다 이때 100번째에 도달했을때 새로운 맵을 추가시키고 만료시간검증을 수행한다
    // 1. 회원가입( putSignUpAuthInfo ) , 비밀번호 초기화( putPwResetAuthInfo )중 하나를 호출합니다
    public void putSignUpAuthInfo(String email, EmailAuthInfo emailAuthInfo) {
        putAuthInfo(signUpMailAuthMapList, signUpMailExpiryCheckList, email, emailAuthInfo);
    }

    public void putPwResetAuthInfo(String email, EmailAuthInfo emailAuthInfo) {
        putAuthInfo(pwResetMailAuthMapList, pwResetMailExpiryCheckList, email, emailAuthInfo);
    }


    private void putAuthInfo(List<ConcurrentHashMap<String, EmailAuthInfo>> authMapList,
        List<EmailAuthInfo> mailExpiryCheckList, String email, EmailAuthInfo emailAuthInfo) {

        int lastIdx = authMapList.size() - 1;
        Map<String, EmailAuthInfo> emailAuthInfoMap = authMapList.get(lastIdx);
        emailAuthInfoMap.put(email, emailAuthInfo);
        // 2. 기본적인 메일인증객체 Map에 추가합니다

        int count = emailAuthInfoMap.size();

        if (count == MAX_ENTRY) {

            mailExpiryCheckList.add(emailAuthInfo);

            // 3. 5000번째( MAX_ENTRY )단위로 선택된 메일인증객체를 만료시간 검사용 객체로 지정을 합니다

            ConcurrentHashMap<String, EmailAuthInfo> newMap = new ConcurrentHashMap<>();

            authMapList.add(newMap);

            // 4. 5000번째 필터링후 authMapList에 새로운 Map을 만들어서 add를 합니다

            cleanupExpiredAuthInfo(authMapList, mailExpiryCheckList);
            // 5. 메일인증객체List의 Map요소들중 최신에 추가된 마지막 Index의 Map에 해당을 검사합니다
            // 이때 검사는 5000번째 단위로 선택된 만료시간 검사용객체를 사용하여 검사
            // 만약 만료시간검사용객체가 만료되었을경우 List의 이전 Map 요소들도 검사할필요없이 모두 제거한다
            // 이렇게 최신의 추가된 Map들이 만료되지않았다면 이전 Map요소도 하나씩 거치면서 검사를 한다
        }

    }

    // 리스트에서 최근에 만들어진 idx를 사용하여 만료시간이 지난 mapList속 Map과 keyList의 key를 삭제한다
    public void cleanupExpiredAuthInfo(List<ConcurrentHashMap<String, EmailAuthInfo>> authMapList,
        List<EmailAuthInfo> mailExpiryCheckList) {
        for (
            int idx = authMapList.size() - 2; idx >= 0; idx--) {
            EmailAuthInfo mailExpiryCheckAuhoInfo = mailExpiryCheckList.get(idx);

            if (mailExpiryCheckAuhoInfo != null && mailExpiryCheckAuhoInfo.isExpired()) {
                for (int j = idx; j >= 0; j--) {
                    authMapList.remove(j);
                    mailExpiryCheckList.remove(j);
                }
                break;
            }
        }
    }


    public Optional<EmailAuthInfo> getSignUpAuthInfo(String email) {
        ListIterator<ConcurrentHashMap<String, EmailAuthInfo>> listIterator = signUpMailAuthMapList.listIterator(
            signUpMailAuthMapList.size());
        while (listIterator.hasPrevious()) {
            Map<String, EmailAuthInfo> previous = listIterator.previous();
            EmailAuthInfo emailAuthInfo = previous.get(email);
            if (emailAuthInfo != null) {
                return Optional.ofNullable(emailAuthInfo);
            }
        }
        return Optional.empty();
    }


    // mail에 해당하는 인증정보를 Map에서 삭제한다
    public void removeSignUpAuthInfo(String email) {
        ListIterator<ConcurrentHashMap<String, EmailAuthInfo>> listIterator = signUpMailAuthMapList.listIterator(
            signUpMailAuthMapList.size());
        while (listIterator.hasPrevious()) {
            Map<String, EmailAuthInfo> previous = listIterator.previous();
            EmailAuthInfo emailAuthInfo = previous.get(email);
            if (emailAuthInfo != null) {
                previous.remove(email);
                return;
            }
        }
    }


    public Optional<EmailAuthInfo> getPwResetAuthInfo(String email) {
        ListIterator<ConcurrentHashMap<String, EmailAuthInfo>> listIterator = pwResetMailAuthMapList.listIterator(
            pwResetMailAuthMapList.size());
        while (listIterator.hasPrevious()) {
            Map<String, EmailAuthInfo> previous = listIterator.previous();
            EmailAuthInfo emailAuthInfo = previous.get(email);
            if (emailAuthInfo != null) {
                return Optional.ofNullable(emailAuthInfo);
            }
        }
        return Optional.empty();
    }


    public void removePwResetAuthInfo(String email) {
        ListIterator<ConcurrentHashMap<String, EmailAuthInfo>> listIterator = pwResetMailAuthMapList.listIterator(
            pwResetMailAuthMapList.size());
        while (listIterator.hasPrevious()) {
            Map<String, EmailAuthInfo> previous = listIterator.previous();
            EmailAuthInfo emailAuthInfo = previous.get(email);
            if (emailAuthInfo != null) {
                previous.remove(email);
                return;
            }
        }
    }

}

