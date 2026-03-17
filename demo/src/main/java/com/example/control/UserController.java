package com.example.control;

import com.example.service.UserTableService;
import com.example.vo.DeleteResultVO;
import com.example.vo.MetaVO;
import com.example.vo.PageVO;
import com.example.vo.UserVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserTableService userTableService;

    public UserController(UserTableService userTableService) {
        this.userTableService = userTableService;
    }

    @GetMapping
    public PageVO<UserVO> list(@RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return userTableService.findPage(page, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserVO> getById(@PathVariable("id") Object id) {
        UserVO data = userTableService.findById(id);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<UserVO> create(@RequestBody UserVO payload) {
        UserVO data = userTableService.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserVO> update(@PathVariable("id") Object id,
                                         @RequestBody UserVO payload) {
        UserVO data = userTableService.update(id, payload);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultVO> delete(@PathVariable("id") Object id) {
        int deleted = userTableService.delete(id);
        return ResponseEntity.ok(new DeleteResultVO(deleted));
    }

    @GetMapping("/meta")
    public MetaVO meta() {
        return userTableService.getMeta();
    }
}
