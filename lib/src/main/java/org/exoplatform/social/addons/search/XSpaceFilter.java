package org.exoplatform.social.addons.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.social.addons.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.space.SpaceFilter;

public class XSpaceFilter extends SpaceFilter {

  private Set<Status> status = new HashSet<>();

  private boolean     includePrivate;

  private boolean     unifiedSearch;

  private boolean     notHidden;

  private boolean     isPublic;

  private boolean     lastAccess;

  private boolean     visited;

  public XSpaceFilter setSpaceFilter(SpaceFilter spaceFilter) {
    if (spaceFilter != null) {
      this.setAppId(spaceFilter.getAppId());
      this.setFirstCharacterOfSpaceName(spaceFilter.getFirstCharacterOfSpaceName());
      this.setIncludeSpaces(spaceFilter.getIncludeSpaces());
      this.setRemoteId(spaceFilter.getRemoteId());
      this.setSorting(spaceFilter.getSorting());
      if (spaceFilter.getSpaceNameSearchCondition() != null) {
        this.setSpaceNameSearchCondition(spaceFilter.getSpaceNameSearchCondition());
      }

      if (spaceFilter instanceof XSpaceFilter) {
        XSpaceFilter filter = (XSpaceFilter) spaceFilter;

        this.setIncludePrivate(filter.isIncludePrivate());
        this.setUnifiedSearch(filter.isUnifiedSearch());
        this.setNotHidden(filter.isNotHidden());
        this.setLastAccess(filter.isLastAccess());
        this.setVisited(filter.isVisited());
        this.addStatus(filter.getStatus().toArray(new Status[filter.getStatus().size()]));
        if (filter.isPublic()) {
          this.setPublic(filter.getRemoteId());
        }
      }
    }
    return this;
  }

  public XSpaceFilter addStatus(Status... st) {
    status.addAll(Arrays.asList(st));
    return this;
  }

  public XSpaceFilter setIncludePrivate(boolean includePrivate) {
    this.includePrivate = includePrivate;
    return this;
  }

  public XSpaceFilter setUnifiedSearch(boolean unifiedSearch) {
    this.unifiedSearch = unifiedSearch;
    return this;
  }

  public XSpaceFilter setNotHidden(boolean notHidden) {
    this.notHidden = notHidden;
    return this;
  }

  public Set<Status> getStatus() {
    return status;
  }

  public boolean isIncludePrivate() {
    return includePrivate;
  }

  public boolean isUnifiedSearch() {
    return unifiedSearch;
  }

  public boolean isNotHidden() {
    return notHidden;
  }

  public void setPublic(String userId) {
    setRemoteId(userId);
    this.isPublic = (userId != null);
  }

  public boolean isPublic() {
    return isPublic;
  }
  
  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  public void setLastAccess(boolean lastAccess) {
    this.lastAccess = lastAccess;
  }

  public boolean isLastAccess() {
    return lastAccess;
  }

  public boolean isVisited() {
    return visited;
  }

}
