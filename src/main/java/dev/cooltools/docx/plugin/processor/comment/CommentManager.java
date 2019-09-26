package dev.cooltools.docx.plugin.processor.comment;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.cooltools.docx.core.Context;
import dev.cooltools.docx.error.ErrorType;
import dev.cooltools.docx.util.RunUtil;

public class CommentManager {
	public class CommentWrapper {
		public Comments.Comment comment;
		private CommentRangeStart commentRangeStart;
		private CommentRangeEnd commentRangeEnd;
		private R lastRun;
		private String commentValue;

		public String getCommentValue() {
			return commentValue;
		}

		public Comments.Comment getComment() {
			return comment;
		}

		public CommentRangeStart getCommentRangeStart() {
			return commentRangeStart;
		}

		public CommentRangeEnd getCommentRangeEnd() {
			return commentRangeEnd;
		}

		public R getLastRun() {
			return lastRun;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(CommentManager.class);
	
	private final Map<BigInteger, Comment> allComments = new HashMap<>();

	public CommentManager(Context context) {
		try {
			this.fetchAllComment(context.getDocument());
		} catch (Docx4JException e) {
			log.error("Could not access document comments", e);
			context.getProblemReporter().reportError(ErrorType.DocumentInitializing, e.getLocalizedMessage());
			allComments.clear();
		}
	}

	private void fetchAllComment(WordprocessingMLPackage document) throws Docx4JException {
		CommentsPart commentsPart = (CommentsPart) document.getParts().get(new PartName("/word/comments.xml"));
		if (commentsPart != null) {
			Comments comments = commentsPart.getContents();
			for (Comments.Comment comment : comments.getComment()) {
				allComments.put(comment.getId(), comment);
			}
		}
	}

	public CommentWrapper getComment(R r) {
		CommentWrapper comment = getCommentAround(r);
		if (comment != null)
			return comment;
		return null;
	}

	public CommentWrapper getCommentAround(R run) {
		if (run instanceof Child) {
			ContentAccessor parent = (ContentAccessor) run.getParent();
			if (parent == null)
				return null;
			CommentWrapper wrapper = new CommentWrapper();
			for (Object contentElement : parent.getContent()) {
				if (contentElement instanceof R) {
					wrapper.lastRun = (R) contentElement;
				}

				// so first we look for the start of the comment
				if (XmlUtils.unwrap(contentElement) instanceof CommentRangeStart) {
					wrapper.commentRangeStart = (CommentRangeStart) contentElement;
				} // so first we look for the start of the comment
				// then we check if the child we are looking for is ours
				else if (run.equals(contentElement)) {
					if (wrapper.commentRangeStart == null)
						return null;
					wrapper.comment = allComments.get(wrapper.commentRangeStart.getId());
				}
				// and then if we have an end of a comment we reset
				else if (XmlUtils.unwrap(contentElement) instanceof CommentRangeEnd) {
					if (wrapper.getComment() != null) {
						wrapper.commentRangeEnd = (CommentRangeEnd) contentElement;
						return wrapper;
					}
					if (((CommentRangeEnd) XmlUtils.unwrap(contentElement)).getId().equals(wrapper.commentRangeStart.getId())) {
						wrapper.commentRangeStart = null;
					}
				}
			}
		}
		return null;

	}

	public static String getCommentString(Comments.Comment comment) {
		StringBuilder builder = new StringBuilder();
		for (Object commentChildObject : comment.getContent()) {
			if (commentChildObject instanceof P) {
				for (Object contentElement : ((P) commentChildObject).getContent()) {
					Object unwrappedObject = XmlUtils.unwrap(contentElement);
					if (unwrappedObject instanceof R) {
						R run = (R) unwrappedObject;
						builder.append(RunUtil.getText(run));
					}
				}
			}
		}
		return builder.toString();
	}

	public static void deleteComment(CommentWrapper comment) {
		if (comment.getCommentRangeEnd() != null) {
			ContentAccessor commentRangeEndParent = (ContentAccessor) comment.getCommentRangeEnd().getParent();
			commentRangeEndParent.getContent().remove(comment.getCommentRangeEnd());
			deleteCommentReference(commentRangeEndParent, comment.getCommentRangeEnd().getId());
		}
		if (comment.getCommentRangeStart() != null) {
			ContentAccessor commentRangeStartParent = (ContentAccessor) comment.getCommentRangeStart().getParent();
			commentRangeStartParent.getContent().remove(comment.getCommentRangeStart());
			deleteCommentReference(commentRangeStartParent, comment.getCommentRangeStart().getId());
		}
	}

	private static void deleteCommentReference(ContentAccessor parent, BigInteger commentId) {
		int index = 0;
		Integer indexToDelete = null;
		for (Object contentObject : parent.getContent()) {
			if (contentObject instanceof R) {
				for (Object runContentObject : ((R) contentObject).getContent()) {
					Object unwrapped = XmlUtils.unwrap(runContentObject);
					if (unwrapped instanceof R.CommentReference) {
						BigInteger foundCommentId = ((R.CommentReference) unwrapped).getId();
						if (foundCommentId.equals(commentId)) {
							indexToDelete = index;
							break;
						}
					}
				}
			}
			index++;
		}
		if (indexToDelete != null) {
			parent.getContent().remove(indexToDelete.intValue());
		}
	}
}
