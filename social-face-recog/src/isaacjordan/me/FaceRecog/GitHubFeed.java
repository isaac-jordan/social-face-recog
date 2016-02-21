package isaacjordan.me.FaceRecog;

import java.util.List;

public class GitHubFeed implements SocialFeed {
	String username;
	
	public GitHubFeed(String username) {
		this.username = username;
	}

	@Override
	public void updateLatestPosts() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Post> getLatestPosts() {
		// TODO Auto-generated method stub
		return null;
	}

}
