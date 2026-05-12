package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.UserProfileDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserFollow;
import es.jadafit.jadafit_api.repository.UserFollowRepository;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SocialService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public SocialService(UserFollowRepository userFollowRepository, UserRepository userRepository, UserService userService) {
        this.userFollowRepository = userFollowRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public void followUser(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ConflictException("No puedes seguirte a ti mismo");
        }

        User follower = userService.getUserById(currentUserId);
        User following = userService.getUserById(targetUserId);

        if (userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new ConflictException("Ya sigues a este usuario");
        }

        UserFollow follow = UserFollow.builder()
                .follower(follower)
                .following(following)
                .build();

        userFollowRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(UUID currentUserId, UUID targetUserId) {
        User follower = userService.getUserById(currentUserId);
        User following = userService.getUserById(targetUserId);

        userFollowRepository.findByFollowerAndFollowing(follower, following)
                .ifPresentOrElse(
                        userFollowRepository::delete,
                        () -> { throw new NotFoundException("No sigues a este usuario"); }
                );
    }

    public List<UserSummaryDTO> getFollowers(UUID userId) {
        User user = userService.getUserById(userId);
        return userFollowRepository.findByFollowing(user).stream()
                .map(follow -> new UserSummaryDTO(follow.getFollower().getId(), follow.getFollower().getUsername(), follow.getFollower().getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public List<UserSummaryDTO> getFollowing(UUID userId) {
        User user = userService.getUserById(userId);
        return userFollowRepository.findByFollower(user).stream()
                .map(follow -> new UserSummaryDTO(follow.getFollowing().getId(), follow.getFollowing().getUsername(), follow.getFollowing().getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public List<UserSummaryDTO> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(user -> new UserSummaryDTO(user.getId(), user.getUsername(), user.getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public UserProfileDTO getUserProfile(UUID targetUserId, UUID currentUserId) {
        User targetUser = userService.getUserById(targetUserId);
        
        long followersCount = userFollowRepository.countByFollowing(targetUser);
        long followingCount = userFollowRepository.countByFollower(targetUser);
        
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            User currentUser = userService.getUserById(currentUserId);
            isFollowing = userFollowRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        }

        return new UserProfileDTO(
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getBio(),
                targetUser.getProfilePictureUrl(),
                followersCount,
                followingCount,
                isFollowing,
                targetUser.getShareProgress()
        );
    }
}
