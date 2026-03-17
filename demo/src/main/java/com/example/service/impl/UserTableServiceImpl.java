package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.service.UserService;
import com.example.vo.MetaVO;
import com.example.vo.PageVO;
import com.example.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class UserTableServiceImpl implements UserService {
    private final UserDao userDao;

    public UserTableServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public PageVO<UserVO> findPage(int page, int pageSize) {
        return userDao.findPage(page, pageSize);
    }

    @Override
    public UserVO findById(Object id) {
        return userDao.findById(id);
    }

    @Override
    public UserVO create(UserVO payload) {
        return userDao.create(payload);
    }

    @Override
    public UserVO update(Object id, UserVO payload) {
        return userDao.update(id, payload);
    }

    @Override
    public int delete(Object id) {
        return userDao.delete(id);
    }

    @Override
    public MetaVO getMeta() {
        return userDao.getMeta();
    }
}
