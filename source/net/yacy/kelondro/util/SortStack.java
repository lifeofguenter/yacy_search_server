// kelondroSortStack.java
// (C) 2008 by Michael Peter Christen; mc@yacy.net, Frankfurt a. M., Germany
// first published 20.02.2008 on http://yacy.net
//
// This is a part of YaCy, a peer-to-peer based web search engine
//
// $LastChangedDate: 2006-04-02 22:40:07 +0200 (So, 02 Apr 2006) $
// $LastChangedRevision: 1986 $
// $LastChangedBy: orbiter $
//
// LICENSE
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package net.yacy.kelondro.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortStack<E> {

    // implements a stack where elements 'float' on-top of the stack according to a weight value.
    // objects pushed on the stack must implement the hashCode() method to provide a handle
    // for a double-check.
    
    private TreeMap<Long, List<E>> onstack; // object within the stack
    private HashSet<E> instack; // keeps track which element has been on the stack
    protected int maxsize;
    
    public SortStack(final int maxsize) {
        // the maxsize is the maximum number of entries in the stack
        // if this is set to -1, the size is unlimited
        this.onstack = new TreeMap<Long, List<E>>();
        this.instack = new HashSet<E>();
        this.maxsize = maxsize;
    }

    public int size() {
        return this.instack.size();
    }
    
    /**
     * put a elememt on the stack using a order of the weight
     * @param element
     * @param weight
     */
    public synchronized void push(final E element, Long weight) {
        if (this.instack.contains(element)) return;
        
        // put the element on the stack
        List<E> l = this.onstack.get(weight);
        if (l == null) {
            l = new LinkedList<E>();
            l.add(element);
            this.onstack.put(weight, l);
        } else {
            l.add(element);
        }
        
        // register it for double-check
        this.instack.add(element);

        // check maximum size of the stack an remove elements if the stack gets too large
        if (this.maxsize <= 0) return;
        while ((this.onstack.size() > 0) && (this.onstack.size() > this.maxsize)) {
            this.onstack.remove(this.onstack.lastKey());
        }
    }
    
    /**
     * return the element with the smallest weight
     * @return
     */
    public synchronized stackElement top() {
        // returns the element that is currently on top of the stack
        if (this.onstack.isEmpty()) return null;
        final Long w = this.onstack.firstKey();
        final List<E> l = this.onstack.get(w);
        final E element = l.get(0);
        return new stackElement(element, w);
    }
    
    /**
     * return the element with the smallest weight and remove it from the stack
     * @return
     */
    public synchronized stackElement pop() {
        // returns the element that is currently on top of the stack
        // it is removed and added to the offstack list
        // this is exactly the same as element(offstack.size())
        if (this.onstack.isEmpty()) return null;
        final Long w = this.onstack.firstKey();
        final List<E> l = this.onstack.get(w);
        final E element = l.remove(0);
        if (l.size() == 0) this.onstack.remove(w);
        this.instack.remove(element);
        return new stackElement(element, w);
    }
    
    public synchronized boolean exists(final E element) {
        // uses the hashCode of the element to find out of the element had been on the list or the stack
        return this.instack.contains(element);
    }
    
    public synchronized void remove(final E element) {
        if (!this.instack.contains(element)) return;
        
        for (Map.Entry<Long,List<E>> entry: this.onstack.entrySet()) {
            Iterator<E> i = entry.getValue().iterator();
            while (i.hasNext()) {
                if (i.next().equals(element)) {
                    i.remove();
                    if (entry.getValue().size() == 0) {
                        this.onstack.remove(entry.getKey());
                    }
                    return;
                }
            }
        }
    }
    
    public synchronized boolean bottom(final long weight) {
        // returns true if the element with that weight would be on the bottom of the stack after inserting
        return weight > this.onstack.lastKey().longValue();
    }
    
    public class stackElement {
        public Long weight;
        public E element;
        public stackElement(final E element, final Long weight) {
            this.element = element;
            this.weight = weight;
        }
        public String toString() {
            return element.toString() + "/" + weight;
        }
    }
}
