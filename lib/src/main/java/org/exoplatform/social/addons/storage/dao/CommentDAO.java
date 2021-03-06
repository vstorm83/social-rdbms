/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.addons.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.addons.storage.entity.ActivityEntity;
import org.exoplatform.social.addons.storage.entity.CommentEntity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 18, 2015  
 */
public interface CommentDAO extends GenericDAO<CommentEntity, Long> {

  /**
   * 
   * @param existingActivity
   * @param offset
   * @param limit
   * @return
   */
  List<CommentEntity> getComments(ActivityEntity existingActivity, int offset, int limit);
  
  /**
   * 
   * @param existingActivity
   * @param sinceTime
   * @param limit
   * @return
   */
  List<CommentEntity> getNewerOfComments(ActivityEntity existingActivity, long sinceTime, int limit);
  
  /**
   * 
   * @param existingActivity
   * @param sinceTime
   * @param limit
   * @return
   */
  List<CommentEntity> getOlderOfComments(ActivityEntity existingActivity, long sinceTime, int limit);

  /**
   * 
   * @param existingActivity
   * @return
   */
  int getNumberOfComments(ActivityEntity existingActivity);
  
  /**
   * Get Activity parent of comment by comment's id
   * @param commentId The comment's id
   * @return
   */
  ActivityEntity findActivity(Long commentId);
}
