package com.synloans.loans.adapter.utils;

import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;

import java.util.*;

@Slf4j
public final class PartyResolveUtils {
    private PartyResolveUtils(){
    }

    public static List<Party> resolveParties(CordaRPCOps proxy, Collection<String> names){
        List<Party> parties = new ArrayList<>(names.size());
        for (String name: names){
            Set<Party> partiesFromName = proxy.partiesFromName(name, true);
            if (partiesFromName.size() > 1){
                log.warn("Несколько кандитатов с именем '{}'", name);
            } else if (partiesFromName.isEmpty()){
                log.warn("Не найден участник с именем '{}'", name);
            } else {
                parties.addAll(partiesFromName);
            }
        }
        return parties;
    }

    public static List<Party> resolveParties(CordaRPCOps proxy, String... names){
        return resolveParties(proxy, Arrays.asList(names));
    }

    public static Optional<Party> resolveParty(CordaRPCOps proxy, String name){
        List<Party> parties = resolveParties(proxy, Collections.singletonList(name));
        if (parties.isEmpty()){
            return Optional.empty();
        } else if (parties.size() > 1){
            log.warn("More than one party found by name='{}', founded={}", name, parties.size());
        }
        return Optional.of(parties.get(0));
    }
}
