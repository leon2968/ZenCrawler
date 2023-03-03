package com.zheng.zencrawler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 The ImageFinder Class responds to a post request with url
 and returns a json file of urls of images found on the url and its subpages
 @author Leon Zheng
 */
@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private ConcurrentHashSet<String> visitedLinks;
	private ConcurrentHashSet<String> linksToVisit;
	private List<String> images = new ArrayList<>();
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String url = req.getParameter("url");
		System.out.println("Got request with URL: " + url);
		images.clear();

		// Initialize the visitedLinks and linksToVisit data structures
		visitedLinks = new ConcurrentHashSet<>();
		linksToVisit = new ConcurrentHashSet<>();

		// Add the URL to the linksToVisit queue
		linksToVisit.add(url);

		// Crawl the website and find all images using ImageFInderCallable
		ExecutorService executorService = Executors.newFixedThreadPool(4); //set to use 4 threads
		List<Future<List<String>>> futures = new ArrayList<>();

		// Keep crawling more pages as long as linksToVisit has elements or a thread is still running
		while(linksToVisit.size()>0 ||  !futures.isEmpty()) {
			if (linksToVisit.size()!=0) {
				ImageFinderCallable ifc = new ImageFinderCallable(linksToVisit.poll(), visitedLinks, linksToVisit);
				//System.out.println("A new thread has started...");
				futures.add(executorService.submit(ifc));
			}

			// Loop through the list of Future objects, add found images and remove any completed tasks
			Iterator<Future<List<String>>> iterator = futures.iterator();
			while (iterator.hasNext()) {
				Future future = iterator.next();
				if (future.isDone()) {
					try {
						List<String> foundImages  = (List<String>) future.get();
						images.addAll(foundImages);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						throw new RuntimeException(e);
					}
					iterator.remove();
				}
			}
		}

		executorService.shutdown();

		// Return the found images as JSON
		System.out.println("Now returning found images..." + images.toString());
		resp.getWriter().print(GSON.toJson(images));
	}
}
