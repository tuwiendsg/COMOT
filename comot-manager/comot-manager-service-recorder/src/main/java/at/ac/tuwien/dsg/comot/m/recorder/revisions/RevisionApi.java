/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.recorder.revisions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.m.recorder.model.LabelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.m.recorder.model.RelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.Revision;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;

@Component
public class RevisionApi {

	private static final Logger LOG = LoggerFactory.getLogger(RevisionApi.class);

	@Autowired
	private ApplicationContext context;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;

	@Autowired
	protected VersionManager versionManager;

	@Transactional
	public void createOrUpdateRegion(Object obj, String regionId, String targetObjectId, String changeType,
			Map<String, Object> changeProperties) throws IllegalAccessException {

		ManagedRegion region = context.getBean(ConverterToInternal.class).convertToGraph(obj);
		LOG.debug("region '{}'count nodes: {}, rels: {}", regionId, region.getNodes().size(), region.getRelationships()
				.size());

		versionManager.insertToDB(region, regionId, targetObjectId, changeType, changeProperties);
	}

	@Transactional
	public void storeEvent(String regionId, String targetObjectId, String changeType,
			Map<String, Object> changeProperties) {
		versionManager.storeChange(regionId, targetObjectId, changeType, changeProperties, System.currentTimeMillis());
	}

	@Transactional
	public Object getRevision(String regionId, String id, Long timestamp) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, RecorderException {

		ManagedRegion region = versionManager.extractFromDB(regionId, id, timestamp);

		if (region.getNodes().isEmpty()) {
			return null;
		}

		ConverterFromInternal converter = context.getBean(ConverterFromInternal.class);

		Object obj = converter.convertToObject(region);
		LOG.debug("getRevision(regionId={}, businessId={}, timestamp={}): {}", regionId, id, timestamp, obj);

		return obj;
	}

	@Transactional
	public Change getAllChanges(String regionId, Long from, Long to) {
		return linkChanges(changeRepo.getAllChangesInRange(regionId, from, to));
	}

	@Transactional
	public Change getAllChangesThatModifiedThisObject(String regionId, String id, Long from, Long to) {

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		// TODO should do this for all identity nodes that were ever connected to this node
		List<Long> list = repo.getAllChangeIdsThatInfluencedIdentityNode(id, from, to);

		return linkChanges(changeRepo.getAllChangesWithTimestampsOrdered(regionId, list));
	}

	protected Change linkChanges(Iterable<Change> iterChanges) {

		List<Change> changes = new ArrayList<>();
		Change thisChange;
		Revision revision;

		for (Change one : iterChanges) {
			changes.add(one);
		}

		// connect changes together
		for (int i = 0; i < changes.size(); i++) {
			thisChange = changes.get(i);

			if (i != 0) {
				revision = changes.get(i - 1).getTo();
				revision.setStart(changes.get(i - 1));
				revision.setEnd(thisChange);
				thisChange.setFrom(revision);

				if ((i + 1) == changes.size()) { // last
					thisChange.getTo().setStart(thisChange);
				}

			} else if (i == 0) { // first
				thisChange.getFrom().setEnd(thisChange);

				if (changes.size() == 1) { // last
					thisChange.getTo().setStart(thisChange);
				}
			}
		}

		return (changes.isEmpty()) ? null : changes.get(0);
	}

	@Transactional
	public boolean verifyObject(String regionId, String id) {

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		if (repo.getRegion() == null) {
			return false;
		}

		if (repo.getIdentityNode(id) == null) {
			return false;
		}

		return true;
	}

	@Transactional
	public List<ManagedObject> getManagedObjects(String regionId) {
		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		List<ManagedObject> list = new ArrayList<>();
		ManagedObject obj;
		Node identityNode;
		Node region = repo.getRegion();

		if (region == null) {
			return list;
		}

		for (Relationship rel : region.getRelationships(Direction.OUTGOING, RelTypes._MANAGE)) {
			identityNode = rel.getEndNode();
			obj = new ManagedObject();

			obj.setId(identityNode.getProperty(InternalNode.ID).toString());

			for (Label label : identityNode.getLabels()) {
				if (!label.name().equals(LabelTypes._IDENTITY.name())) {
					obj.setLabel(label.name());
					break;
				}
			}
			list.add(obj);

		}

		return list;
	}

}
