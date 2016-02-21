package isaacjordan.me.FaceRecog;


import java.util.List;

import org.eclipse.egit.github.core.client.GitHubClient;


public class GitHubFeed implements SocialFeed {
	private static final long serialVersionUID = -767532415720538684L;
	String username;
	List<Post> latestPosts = null;
	
	public GitHubFeed(String username) {
		this.username = username;
	}

	@Override
	public void updateLatestPosts() {
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public List<Post> getLatestPosts() {
		// TODO Auto-generated method stub
		return null;
	}

}
