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
package org.exoplatform.social.addons.storage.dao.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.social.addons.storage.dao.RelationshipDAO;
import org.exoplatform.social.addons.storage.dao.jpa.query.RelationshipQueryBuilder;
import org.exoplatform.social.addons.storage.dao.jpa.synchronization.SynchronizedGenericDAO;
import org.exoplatform.social.addons.storage.entity.RelationshipItem;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 4, 2015  
 */
public class RelationshipDAOImpl extends SynchronizedGenericDAO<RelationshipItem, Long> implements RelationshipDAO {

  @Override
  public long count(Identity identity, Type status) {
    return RelationshipQueryBuilder.builder()
                                        .owner(identity)
                                        .status(status)
                                        .buildCount()
                                        .getSingleResult();
  }

  @Override
  public RelationshipItem getRelationship(Identity identity1, Identity identity2) {
    TypedQuery<RelationshipItem> query = RelationshipQueryBuilder.builder()
                                                                 .sender(identity1)
                                                                 .receiver(identity2)
                                                                 .buildSingleRelationship();
    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<RelationshipItem> getRelationships(Identity identity, Type type, long offset, long limit) {
    return RelationshipQueryBuilder.builder()
                                   .owner(identity)
                                   .status(type)
                                   .offset(offset)
                                   .limit(limit)
                                   .build()
                                   .getResultList();
  }

  @Override
  public int getRelationshipsCount(Identity identity, Type type) {
    return RelationshipQueryBuilder.builder()
                                   .owner(identity)
                                   .status(type)
                                   .buildCount()
                                   .getSingleResult()
                                   .intValue();
  }

  @Override
  public List<RelationshipItem> getLastConnections(Identity identity, int limit) {
    return RelationshipQueryBuilder.builder()
                                   .owner(identity)
                                   .status(Relationship.Type.CONFIRMED)
                                   .offset(0)
                                   .limit(limit)
                                   .buildLastConnections()
                                   .getResultList();
  }

}