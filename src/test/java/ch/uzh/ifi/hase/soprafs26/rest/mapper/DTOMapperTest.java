package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("name");
		userPostDTO.setUsername("username");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getName(), user.getName());
		assertEquals(userPostDTO.getUsername(), user.getUsername());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getName(), userGetDTO.getName());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

    @Test
    public void testCreateUser_fromUserPostDTO_toUser_withBioAndGenres_success() {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setName("name");
        userPostDTO.setUsername("username");
        userPostDTO.setBio("I love reading");
        userPostDTO.setGenres(java.util.List.of("Fantasy", "Thriller"));

        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        assertEquals(userPostDTO.getName(), user.getName());
        assertEquals(userPostDTO.getUsername(), user.getUsername());
        assertEquals(userPostDTO.getBio(), user.getBio());
        assertEquals(userPostDTO.getGenres(), user.getGenres());
    }

    @Test
    public void testConvertToUserStatsGetDTO_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setTotalPoints();
        leaderboard.addReadingPoints(40L);
        leaderboard.addQuizzPoints(20L);

        UserStatsGetDTO dto = DTOMapper.INSTANCE.convertToUserStatsGetDTO(user, leaderboard);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(0, dto.getBooksRead());
        assertEquals(0, dto.getPagesRead());
        assertEquals(0, dto.getNumFriends());
        assertEquals(60L, dto.getTotalPoints());
    }
}
