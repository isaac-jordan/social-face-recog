package isaacjordan.me.FaceRecog;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterFeed implements SocialFeed {
	private static final long serialVersionUID = 7517072502660303038L;
	String username;
	Twitter twitter;
	List<Post> latestPosts;
	
	public TwitterFeed(String username) {
		this.username = username;
		latestPosts = new ArrayList<Post>();
	}

	@Override
	public void updateLatestPosts() {
		// TODO Auto-generated method stub
		twitter = TwitterFactory.getSingleton();
		Paging paging = new Paging(1, 5);
		List<Status> statuses = null;
		try {
			System.out.println("USERNAME: " + username);
			statuses = twitter.getUserTimeline(username, paging);
			System.out.println(statuses);
		} catch (TwitterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (latestPosts == null) {
			latestPosts = new ArrayList<Post>();
		}
	    
		latestPosts.clear();
	    for (Status status : statuses) {
	    	Post post = new Post();
	    	post.summary = status.getText();
	        latestPosts.add(post);
	    }
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public List<Post> getLatestPosts() {
		return latestPosts;
	}
	
	
}
