package ewm.user.mapper;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ewm.user.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(NewUserRequest dto);

    UserResponse toDto(User user);
}
