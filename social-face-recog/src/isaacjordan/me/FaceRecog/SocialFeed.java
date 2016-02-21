package isaacjordan.me.FaceRecog;

import java.io.Serializable;
import java.util.List;

public interface SocialFeed extends Serializable {
	public void updateLatestPosts();
	
	public String getUsername();
	
	public List<Post> getLatestPosts();
}
