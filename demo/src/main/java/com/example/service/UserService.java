package com.example.service;

import com.example.vo.MetaVO;
import com.example.vo.PageVO;
import com.example.vo.UserVO;

public interface UserService {
    PageVO<UserVO> findPage(int page, int pageSize);

    UserVO findById(Object id);

    UserVO create(UserVO payload);

    UserVO update(Object id, UserVO payload);

    int delete(Object id);

    MetaVO getMeta();
}
