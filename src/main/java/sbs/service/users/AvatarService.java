package sbs.service.users;

import org.springframework.core.io.Resource;

public interface AvatarService {

	Resource getAnonymousPicture();
	Resource getAvatarResourceByUsername(String username);
	Resource getAvatarResourceById(Long id);

}