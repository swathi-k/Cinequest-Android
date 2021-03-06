/*
    Copyright 2008 San Jose State University
    
    This file is part of the Blackberry Cinequest client.

    The Blackberry Cinequest client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Blackberry Cinequest client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Blackberry Cinequest client.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.sjsu.cinequest.comm;

import java.io.IOException;
import java.util.Vector;

import org.xml.sax.SAXException;

import android.util.Log;
import edu.sjsu.cinequest.comm.cinequestitem.Festival;
import edu.sjsu.cinequest.comm.xmlparser.FestivalParser;
import edu.sjsu.cinequest.comm.xmlparser.NewsFeedParser;

/**
 * @author Kevin Ross (cs160_109)
 */
public class QueryManager {
	
	private String lastUpdated="";

	private static final String imageBase = "http://mobile.cinequest.org/";
	private static final String mainImageURL = "imgs/mobile/creative.gif";
	
	public static final String showsFeedURL = "http://payments.cinequest.org/websales/feed.ashx?guid=70d8e056-fa45-4221-9cc7-b6dc88f62c98&showslist=true";
	public static final String newsFeedURL = "http://www.cinequest.org/news.php";
	public static final String venuesFeedURL = "http://www.cinequest.org/venuelist.php";
	
	private Festival festival = new Festival();
	private Object festivalLock = new Object();
	private boolean festivalQueryInProgress = false;
	private Object progressLock = new Object();
	
	private interface Callable {
		Object run() throws Throwable;
	}

	private void getWebData(final Callback callback, final Callable task) {
		if (callback == null || task == null)
			throw new NullPointerException();
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Object result = task.run();
					Platform.getInstance().invoke(callback, result);
				} catch (Throwable e) {
					Platform.getInstance().failure(callback, e);
				}
			}
		});
		t.start();
	}

	public void prefetchFestival() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Platform.getInstance().log("Prefetching festival");
					getFestival(new Callback() {
						public void failure(Throwable t) {
							Platform.getInstance().log(t);
						}

						public void invoke(Object result) {
						}

						public void starting() {
						}
					});
					Platform.getInstance().log("Done prefetching festival");
				} catch (Exception e) {
					Platform.getInstance().log(e);
				}
			}
		});
		t.start();
	}
	
	public String getlastUpdated() {
		return lastUpdated;
	}

	public void getAllFilms(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getFilms();
			}
		});
	}
	public void getAllEvents(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getEvents();
			}
		});
	}
	
	public void getAllForums(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getForums();
			}
		});
	}
	
	public void getAllEventsAndForums(final Callback callback){
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				Festival festival = getFestival(callback);
				Vector vt = new Vector(festival.getEvents());
				vt.addAll(festival.getForums());
				return vt;
			}
		});
	}

	public void getFilmDates(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getFilmDates();
			}
		});
	}
	
	public void getEventDates(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getEventDates();
			}
		});
	}
	
	public void getForumDates(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getForumDates();
			}
		});
	}
	public void getFilmsByDate(final String date, final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getFilmsByDateGroupedByTime(date);
			}
		});
	}
	
	public void getEventsByDate(final String date, final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getEventsByDateGroupedByTime(date);
			}
		});
	}
	
	public void getForumsByDate(final String date, final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback).getForumsByDateGroupedByTime(date);
			}
		});
	}
	
	public void getCommonItem(final Callback callback, final int id) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {				
				return getFestival(callback).getCommonItemUsingId(id);
			}
		});
					
	}

	/**
	 * Gets a special screen as a vector of Section objects
	 * 
	 * @param type
	 *            one of "home", "info", "offseason", "offseasoninfo",
	 *            "release", "pick"
	 * @param callback
	 *            returns the result
	 */
	public void getSpecialScreen(final String type, final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				
				return NewsFeedParser.parseNewsFeed(newsFeedURL, callback);								
			}
		});
	}


	/**
	 * Resolves a relative image URL
	 * 
	 * @param url
	 *            the relative URL to resolve
	 * @return the absolute URL for fetching the image
	 */
	public String resolveRelativeImageURL(String url) {
		if (url.startsWith("http://"))
			return url;
		else
			return imageBase + url;
	}

	/**
	 * Gets the URL for the main image (in the entry screen)
	 */
	public String getMainImageURL() {
		return resolveRelativeImageURL(mainImageURL);
	}
	
	public void loadFestival(final Callback callback) {
		getWebData(callback, new Callable() {
			public Object run() throws Throwable {
				return getFestival(callback);
			}
		});
	}
	
	/**
	 * Gets the complete data of the Festival. Call only inside run method of
	 * getWebData.
	 */
	private Festival getFestival(final Callback callback) throws SAXException,
			IOException {
		synchronized (progressLock) {
			if (festivalQueryInProgress)
				Platform.getInstance()
						.log("Festival query called while another query in progress.");
			Platform.getInstance().starting(callback);
		}
		synchronized (festivalLock) {
			
			String updatedDate = NewsFeedParser.getLastpdated(newsFeedURL, callback);			
			Log.i("QueryManager:getFestival-Date Check:","UpdatedDate from News Feed:"+updatedDate+" lastUpdated:"+lastUpdated);
			
			if (updatedDate.equalsIgnoreCase(lastUpdated) && (!festival.isEmpty()))
				return festival;
			synchronized (progressLock) {
				festivalQueryInProgress = true;
			}
			try {
				
				// Using the new Xml feed.
				// FIXME - Should the URL be hardcoded over here.
				Festival result = FestivalParser.parseFestival(showsFeedURL, callback);
				if (!result.isEmpty()) {
					festival = result;
					lastUpdated=updatedDate;					
				} else {
					Log.i("QueryManager:getFestival","Festival Object is Empty");
				}

			} finally {
				synchronized (progressLock) {
					festivalQueryInProgress = false;
				}
			}
			return festival;
		}
	}
}
