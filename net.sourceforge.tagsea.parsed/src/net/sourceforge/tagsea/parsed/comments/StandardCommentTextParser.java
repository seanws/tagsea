/*******************************************************************************
 * Copyright 2005-2006, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Chisel Group, University of Victoria
 *******************************************************************************/
package net.sourceforge.tagsea.parsed.comments;

import java.io.IOException;
import java.util.LinkedList;

import net.sourceforge.tagsea.parsed.comments.MalformedWaypointException.ErrorType;

import org.eclipse.jface.text.Region;

/**
 * Parses a string to find java waypoint info.
 * @author Del Myers
 */
public class StandardCommentTextParser  {
	public static final String TAG_SEQUENCE = "@tag"; //$NON-NLS-1$
	private int position;
	private int wordPosition;

	private StandardCommentTextParser() {}
	
	/**
	 * Reads the given text to discover a single waypoint. Assumes a single line that only contains
	 * waypoint text. If a MalformedWaypointException is thrown during the parse, its offset will
	 * be relative to the beginning of the text supplied <i>not</i> the offset supplied.
	 * @param waypointDefinition the text that represents the waypoint.
	 * @param offset the beginning offset of the waypoint in the original document.
	 * @param line the line at which the waypoint starts in the original document.
	 * @return
	 * throws MalformedWaypointException if an exception was thrown during the parse.
	 */
	public static StandardCommentWaypointDescriptor parse(String waypointDefinition, int offset, int line) throws MalformedWaypointException, IOException {
		return new StandardCommentTextParser().internalParse(waypointDefinition, offset, line);
	}
	
	private StandardCommentWaypointDescriptor internalParse(String waypointDefinition, int offset, int line) throws MalformedWaypointException, IOException{
		//read ahead until we get to a "

		position = 0;
		waypointDefinition = chomp(waypointDefinition);
		StandardCommentWaypointDescriptor info = new StandardCommentWaypointDescriptor(waypointDefinition, offset, line);
		readToTag(waypointDefinition);
		Region[] words = getWords(waypointDefinition);
		interpretWords(words, waypointDefinition, info);
		String comment = ""; //$NON-NLS-1$
		if (position < waypointDefinition.length()) {
			comment = waypointDefinition.substring(position).trim();
		}
		info.setMessage(comment, new Region(position, comment.length()));
		return info;

		
	}

	/**
	 * @param waypointDefinition
	 * @return
	 */
	private String chomp(String waypointDefinition) {
		int index = waypointDefinition.length()-1;
		while (Character.isWhitespace(waypointDefinition.charAt(index)) && index >= 0) {
			index--;
		}
		if (index >= -1) {
			waypointDefinition = waypointDefinition.substring(0, index+1);
		}
		return waypointDefinition;
	}

	/**
	 * @param words
	 * @param waypointDefinition
	 * @param info 
	 * @throws MalformedWaypointException 
	 */
	private void interpretWords(Region[] words, String waypointDefinition, StandardCommentWaypointDescriptor info) throws MalformedWaypointException {
		for (Region region : words) {
			String word = waypointDefinition.substring(region.getOffset(), region.getOffset()+region.getLength());
			if ("@tag".equals(word)) throw new MalformedWaypointException(ErrorType.BadTag, ErrorType.BadTag.toString(), region.getOffset(), region.getLength());
			if (word.startsWith("-")) { //$NON-NLS-1$
				interpretMetaData(word, region, info);
			} else { 
				try {
					interpretTag(word, region, info);
				} catch (IOException e) {
					
				}
			}
		}
	}

	/**
	 * @param word
	 * @param region
	 * @param info
	 * @throws MalformedWaypointException 
	 * @throws IOException 
	 */
	private void interpretTag(String word, Region region, StandardCommentWaypointDescriptor info) throws MalformedWaypointException, IOException {
		int open = word.indexOf('(');
		int close = word.lastIndexOf(')');
		int dot = word.indexOf('.');
		
		if (dot != -1 && (open != -1 || close != -1)) {
			throw new MalformedWaypointException(ErrorType.BadTag, ErrorType.BadTag.toString(), region.getOffset(), region.getLength());
		}
		if (close != -1 && close != word.length()-1) {
			throw new MalformedWaypointException(ErrorType.MisplacedParens, ErrorType.MisplacedParens.toString(), region.getOffset(), region.getLength());
		}
		if (word.startsWith(".")) {
			throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString()+": .", region.getOffset(), 1);
		}
		if (word.endsWith(".")) {
			throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString()+ ": .", region.getOffset() + region.getLength()-1, 1);
		}
		if (open != -1) {
			interpretParenTag(word, region, info);
		} else {
			info.putTag(word, region);
		}
	}

	/**
	 * @param word
	 * @param region
	 * @param info
	 * @throws IOException 
	 * @throws MalformedWaypointException 
	 */
	private void interpretParenTag(String word, Region region, StandardCommentWaypointDescriptor info) throws IOException, MalformedWaypointException {
		LinkedList<String> tags = new LinkedList<String>();
		wordPosition = 0;
		tags.add(findTags(word,region.getOffset()));
		for (String s : tags) {
			info.putTag(s, region);
		}
	}

	/**
	 * @param word
	 * @param start where to start reading the word.
	 * @param offset the offset into the tag data at which the word occurs.
	 * @return
	 * @throws IOException 
	 * @throws MalformedWaypointException 
	 */
	private String findTags(String reader, int localOffset) throws IOException, MalformedWaypointException {
		char c;
		String currentWord = ""; //$NON-NLS-1$
		boolean done = wordPosition >= reader.length();
		while (!done) {
			 c = reader.charAt(wordPosition); 
			if (Character.isWhitespace(c)) {
				if (!"".equals(currentWord)) { //$NON-NLS-1$
					//unexpected white space
					throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": whitespace", wordPosition+localOffset, 1);
				}
				//skip it.
			} else {
				if (c == '(') {
					if ("".equals(currentWord))  //$NON-NLS-1$
						throw new MalformedWaypointException(ErrorType.ExpectedWord, ErrorType.ExpectedWord.toString(), wordPosition+localOffset, 1);
					wordPosition++;
					String nextWord = findTags(reader, localOffset);
					if (nextWord.length() > 0) {
						currentWord += "." + nextWord; //$NON-NLS-1$
					}
					done = true;
				} else if (c == ')') {
					if ("".equals(currentWord)) //$NON-NLS-1$
						throw new MalformedWaypointException(ErrorType.ExpectedWord, ErrorType.ExpectedWord.toString(), wordPosition+localOffset, 1);
					return currentWord;
				} else
					currentWord += c;
			}
			wordPosition++;
		}
		return currentWord;
	}

	/**
	 * @param word
	 * @param region
	 * @param info
	 * @throws MalformedWaypointException 
	 */
	private void interpretMetaData(String word, Region region, StandardCommentWaypointDescriptor info) throws MalformedWaypointException {
		int equals = word.indexOf('=');
		if (equals < 0) {
			throw new MalformedWaypointException(ErrorType.ExpectedEquals, ErrorType.ExpectedEquals.toString(), region.getOffset(), region.getLength());
		} else if (equals == 0) {
			throw new MalformedWaypointException(ErrorType.ExpectedLValue, ErrorType.ExpectedLValue.toString(), region.getOffset(), 1);
		} else if (equals == word.length()-1) {
			throw new MalformedWaypointException(ErrorType.ExpectedRValue, ErrorType.ExpectedRValue.toString(), region.getOffset(), word.length());
		}
		String key = word.substring(1, equals);
		String value = word.substring(equals+1, word.length());
		if (value.startsWith("\"")) { //$NON-NLS-1$
			value = value.substring(1, value.length()-1);
		}
		info.putAttribute(key, value, region);
	}

	/**
	 * @param reader
	 * @return
	 * @throws IOException 
	 * @throws MalformedWaypointException 
	 */
	private Region[] getWords(String reader) throws IOException, MalformedWaypointException {
		LinkedList<Region> words = new LinkedList<Region>();
		char c = 0;
		String currentWord = ""; //$NON-NLS-1$
		int wordStart = position;
		boolean inQuotes =false;
		int parensCount = 0;
		boolean isMetaData = false;
		char lastCharacter = ' ';
		boolean hasPeriod = false;
		while (position < reader.length()) {
			lastCharacter = c;
			c = (char)reader.charAt(position);
			position++;
			if (Character.isWhitespace(c)) {
				if (!"".equals(currentWord)) { //$NON-NLS-1$
					if (isMetaData) {
						if (!inQuotes) {
							words.add(new Region(wordStart, currentWord.length()));
							isMetaData = false;
							currentWord = ""; //$NON-NLS-1$
							wordStart = position;
							hasPeriod = false;
							parensCount = 0;
						}  else {
							currentWord += c;
						}
					} else {
						if (parensCount == 0) {
							if (currentWord.endsWith(".")) { //$NON-NLS-1$
								//trailing period is bad.
								throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": '.'", wordStart+currentWord.length()-1, 1); //$NON-NLS-1$
							}
							words.add(new Region(wordStart, currentWord.length()));
							isMetaData = false;
							currentWord = ""; //$NON-NLS-1$
							hasPeriod = false;
							wordStart = position;
						} else {
							currentWord += c;
						}
					}
				} else {
					wordStart = position;
				}
			} else {
				if (c == '-' && Character.isWhitespace(lastCharacter)) {
					isMetaData = true;
				} else if (c == '(' && !isMetaData) {
					parensCount++;
					if (hasPeriod) {
						throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": \'"+c+"\'", position-1, 1); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (c == ')' && !isMetaData) {
					parensCount--;
					if (hasPeriod) {
						throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": \'"+c+"\'", position-1, 1); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				if (c == '"' && !isMetaData) {
					throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": \'"+c+"\'", position-1, 1); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (isMetaData && c == '\"') {
					inQuotes = !inQuotes;
					if (position < reader.length() && !inQuotes) {
						if (!Character.isWhitespace(reader.charAt(position))) {
							throw new MalformedWaypointException(ErrorType.ExpectedSpace, ErrorType.ExpectedSpace.toString(), position-1, 1);
						}
					}
				} else if (c == ':' && parensCount != 0) {
					throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": \'"+c+"\'", position-1, 1);					 //$NON-NLS-1$ //$NON-NLS-2$
				} else if (c=='.' && !isMetaData) {
					 if (parensCount != 0) {
						 throw new MalformedWaypointException(ErrorType.IllegalCharacter, ErrorType.IllegalCharacter.toString() + ": \'"+c+"\'", position-1, 1); //$NON-NLS-1$ //$NON-NLS-2$
					 }
					 hasPeriod = true;
				} else if (c == ':' && !inQuotes) {
					//quit, we have reached the end.
					break;
				}
				currentWord += c;
			}
		} 
		if (inQuotes) {
			throw new MalformedWaypointException(ErrorType.ExpectedQuote, ErrorType.ExpectedQuote.toString(), wordStart, currentWord.length());
		} else if (parensCount != 0) {
			throw new MalformedWaypointException(ErrorType.UnmatchedParen, ErrorType.UnmatchedParen.toString(), wordStart, currentWord.length());
		} else if (!"".equals(currentWord)) { //$NON-NLS-1$
			words.add(new Region(wordStart, currentWord.length()));
		}
 		return (Region[])words.toArray(new Region[words.size()]);
	}

	/**
	 * @param reader
	 * @throws IOException 
	 */
	private void readToTag(String reader) throws IOException {
		String target = TAG_SEQUENCE;
		String currentWord = ""; //$NON-NLS-1$
		position = 0;
		while (position < reader.length()) {
			char c = reader.charAt(position);
			position++;
			if (Character.isWhitespace(c)) {
				currentWord = ""; //$NON-NLS-1$
				continue;
			} else {
				currentWord = currentWord + c;
				if (currentWord.equals(target)) {
					if (position < reader.length() && Character.isWhitespace(reader.charAt(position)))
						break;
					else 
						currentWord = ""; //$NON-NLS-1$
				}
			}
		}
		
	}

}
