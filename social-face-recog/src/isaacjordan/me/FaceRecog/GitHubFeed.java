package isaacjordan.me.FaceRecog;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHUserSearchBuilder;
import org.kohsuke.github.GitHub;

import twitter4j.Twitter;


public class GitHubFeed implements SocialFeed {
	private static final long serialVersionUID = 2612602669245273846L;
	GitHub github = null;
	String username;
	List<Post> latestPost;
	
	public GitHubFeed(String username) {
		this.username = username;
		latestPost = new ArrayList<Post>();	
	}
	
	@Override
	public void updateLatestPosts() {
		// Commented out due to missing libraries
		/*try {
			github = GitHub.connect();
		} catch (IOException e) {
			System.out.println(e);
		}
		GHUserSearchBuilder search = github.searchUsers();
		GHUserSearchBuilder results = search.repos(username);
		Post post = new Post();
		post.summary = results.toString();
		latestPost.add(post);*/
	}
	@Override
	public String getUsername() {
		return username;
	}
	@Override
	public List<Post> getLatestPosts() {
		return latestPost;
	}

	@Override
	public int getFollowerCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFollowingCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
