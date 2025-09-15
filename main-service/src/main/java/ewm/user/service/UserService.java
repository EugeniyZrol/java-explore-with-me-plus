package ewm.user.service;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService{
    UserResponse createUser(NewUserRequest userRequest);

    List<UserResponse> getUsers(List<Long> ids, Pageable pageable);

    void deleteUser(Long userId);
}