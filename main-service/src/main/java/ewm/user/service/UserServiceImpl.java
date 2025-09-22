package ewm.user.service;

import ewm.user.dto.UserShortDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserResponse;
import ewm.user.mapper.UserMapper;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import ewm.exception.NotFoundException;
import ewm.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(NewUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ConflictException("User with email already exists: " + userRequest.getEmail());
        }

        User user = userMapper.toEntity(userRequest);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(List<Long> ids, Pageable pageable) {
        return userRepository.findUsersByIds(ids, pageable)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserShortDto getUserById(Long userId) {
        checkUserExists(userId);
        return userMapper.toShortDto(userRepository.findById(userId).get());
    }

    @Override
    public void deleteUser(Long userId) {
        checkUserExists(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }
}
