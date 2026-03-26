package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "token", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "favGenre", ignore = true)
	@Mapping(target = "leaderboard", ignore = true)
	@Mapping(target = "friends", ignore = true)
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "user.id", target = "id")
	@Mapping(source = "user.username", target = "username")
	@Mapping(source = "user.booksRead", target = "booksRead")
	@Mapping(source = "user.pagesRead", target = "pagesRead")
	@Mapping(source = "user.numFriends", target = "numFriends")
	@Mapping(source = "leaderboard.totalPoints", target = "totalPoints")
	UserStatsGetDTO convertToUserStatsGetDTO(User user, Leaderboard leaderboard);
}
