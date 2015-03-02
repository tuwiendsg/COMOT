package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class UtilsLc {

	private static final Logger log = LoggerFactory.getLogger(UtilsLc.class);

	public static CloudService removeProviderInfo(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setOsu(null);
		}
		return service;
	}

	public static StateMessage stateMessage(Message message) throws UnsupportedEncodingException, JAXBException {
		StateMessage msg = Utils.asStateMessage(new String(message.getBody(), "UTF-8"));
		return msg;
	}

	public static Set<Action> allPossibleActions(State state) {
		Set<Action> actions = new HashSet<>();

		for (Action action : Action.values()) {
			if (state.execute(action) != null) {
				actions.add(action);
			}
		}

		log.debug("allPossibleActions(state={}) : {}", state, actions);
		return actions;
	}

	public static State translateToOldState(Action action, State newState) {

		State result = null;
		for (State state : State.values()) {
			if ((result = state.execute(action)) != null) {
				if (result.equals(newState)) {
					break;
				}
			}
		}

		log.debug("translateToOldState(action={}, newState={}) : {}", action, newState, result);
		return result;
	}

	public static Action translateToAction(State oldState, State newState) {

		State temp;
		Action result = null;
		for (Action action : Action.values()) {
			if ((temp = oldState.execute(action)) != null) {
				if (temp.equals(newState)) {
					result = action;
					break;
				}
			}
		}

		log.debug("translateToAction(oldState={}, newState={}) : {}", oldState, newState, result);
		return result;
	}

}
