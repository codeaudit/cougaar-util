package org.cougaar.tools.server.system;

import java.io.Serializable;
import java.util.List;

/**
 * A status summary of a running process, which may or may
 * not be a Java Virtual Machine, which includes references
 * to parent/child processes and basic process information.
 * <p>
 * Some of this information is Operating System specific,
 * such as the ability to track memory/cpu usage.
 */
public final class ProcessStatus 
implements Serializable {

  /**
   * Marking disabled or there is no relation with this
   * ProcessStatus and the self-"marked" ProcessStatus.
   *
   * @see #getMark()
   */
  public static final int MARK_NONE = 0;

  /**
   * This ProcessStatus is the single "marked" ProcessStatus.
   *
   * @see #getMark()
   */
  public static final int MARK_SELF = 1;

  /**
   * This ProcessStatus contains the "marked" ProcessStatus as 
   * a child or (great+)grandchild.
   *
   * @see #getMark()
   */
  public static final int MARK_PARENT = 2;

  /**
   * This ProcessStatus has the "marked" ProcessStatus as a 
   * parent or (great+)grandparent.
   *
   * @see #getMark()
   */
  public static final int MARK_CHILD = 3;

  /**
   * @see #getMark()
   */
  public static final String[] MARK_STRINGS = 
    new String[] {
      "none",
      "self",
      "parent",
      "child",
    };

  //
  // relation to other ProcessStatus entries
  //

  private final ProcessStatus parent;
  private final List children;
  private int mark = MARK_NONE;

  //
  // details
  //

  private final long pid;
  private final long ppid;
  private final long start;
  private final String user;
  private final String cmd;

  //
  // plenty can be added here... see "man ps"
  //

  public ProcessStatus(
      ProcessStatus parent,
      List children,
      long pid, 
      long ppid, 
      long start, 
      String user,
      String cmd) {
    this.parent = parent;
    this.children = children;
    this.pid = pid;
    this.ppid = ppid;
    this.start = start;
    this.user = user;
    this.cmd = cmd;
  }

  //
  // tree-relation to other ProcessStatus entries:
  //

  /**
   * Get the parent process's <code>ProcessStatus</code> of
   * this process.
   *
   * Most <code>ProcessStatus</code> data structures will have
   * a non-null <tt>getParent()</tt>.  The important exceptions 
   * are:<ul>
   *   <li>the root 
   *       <tt>(getParentProcessIdentifier() == 0)</tt>.</li>
   *   <li>a trimmed listing, where not all ProcessStatus
   *       entries are gathered.</li>
   * </ul>
   *
   * @see #getParentProcessIdentifier()
   */
  public ProcessStatus getParent() {
    return parent;
  }

  /**
   * Get an immutable list of children <code>ProcessStatus</code> 
   * processes.
   */
  public List getChildren() {
    return children;
  }

  //
  // details:
  //
  
  /**
   * Get the process id.
   */
  public long getProcessIdentifier() {
    return pid;
  }

  /**
   * Get the parent's process id.
   *
   * @see #getParent()
   */
  public long getParentProcessIdentifier() {
    return ppid;
  }

  /**
   * Get the start time in milliseconds.
   */
  public long getStartTime() {
    return start;
  }

  /**
   * Get the user name.
   */
  public String getUserName() {
    return user;
  }

  /**
   * Get the command.
   * <p>
   * The result may contain whitespace and other control characters.
   */
  public String getCommand() {
    return cmd;
  }

  //
  // mark utilities:
  //
  
  /**
   * Get the "mark" of this process in relation to the process
   * that has the "MARK_SELF" tag.
   * <p>
   * This is a utility method for recursively tagging the
   * process tree.  For example, the result of a 
   * <code>ProcessStatusReader</code> could mark the JVM as the
   * "MARK_SELF", it's parent processes as "MARK_PARENT", and
   * all child processes as "MARK_CHILD".  This facilitates
   * the later display of this information.
   *
   * @see #mark()
   */
  public int getMark() {
    return mark;
  }

  /**
   * Get the mark as a readable String.
   * 
   * @see #getMark()
   */
  public String getMarkAsString() {
    return MARK_STRINGS[mark];
  }

  /**
   * Mark this ProcessStatus as "MARK_NONE".
   * <p>
   * This is useful when preparing to call <tt>mark()</tt>.  Note
   * that for a clean "mark()" all ProcessStatus data structures
   * must be first "unmark()"ed.
   */
  public void unmark() {
    this.mark = MARK_NONE;
  }

  /**
   * Mark this ProcessStatus with "MARK_SELF",
   * it's parents and (grand+)parents with "MARK_PARENT", and
   * all it's children and (grand+)children with "MARK_CHILD".
   * <p>
   * This does <b>not</b> set unrelated ProcessStatus data 
   * structures to "MARK_NONE" -- see <tt>unmark()</tt>.
   *
   * @see #getMark()
   * @see #unmark()
   */
  public void mark() {
    // mark self
    this.mark = MARK_SELF;

    // mark parents
    for (ProcessStatus p = this.parent;
        p != null;
        p = p.parent) {
      p.mark = ProcessStatus.MARK_PARENT;
    }

    // mark children
    this.markChildren();
  }

  // recursive!
  private void markChildren() {
    List cl = this.children;
    int ncl = cl.size();
    for (int i = 0; i < ncl; i++) {
      ProcessStatus ci = (ProcessStatus)cl.get(i);
      ci.mark = MARK_CHILD;
      ci.markChildren();
    }
  }

  public String toString() {
    return toString(true);
  }

  public String toString(boolean showFullDetails) {
    StringBuffer buf = new StringBuffer();
    
    buf.append("pid=").append(getProcessIdentifier());

    if (showFullDetails) {
      buf.append(" mark=").append(getMarkAsString());

      buf.append(" parent={");
      if (getParent() != null) {
        buf.append(getParent().getProcessIdentifier());
      }
      buf.append("}");

      List c = getChildren();
      int n = c.size();
      buf.append(" children[").append(n).append("]={");
      for (int i = 0; i < n; i++) {
        ProcessStatus ci = (ProcessStatus)c.get(i);
        buf.append(ci.getProcessIdentifier());
        if (i < (n - 1)) {
          buf.append(", ");
        }
      }
      buf.append("}");

      buf.append(" ppid=").append(getParentProcessIdentifier());

      java.util.Date d = new java.util.Date(getStartTime());
      buf.append(" start=\"").append(d).append("\"");

      buf.append(" user=\"").append(getUserName()).append("\"");
    }

    buf.append(" cmd=\"").append(getCommand()).append("\"");

    return buf.toString();
  }
}
