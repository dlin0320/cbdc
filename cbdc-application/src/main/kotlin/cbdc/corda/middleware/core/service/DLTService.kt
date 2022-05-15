package cbdc.corda.middleware.core.service

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party

/**
 * Interface that abstracts common features in Corda and Fabric. The implementation for each DLT platform is currently
 * in different repositories; having this interface makes direct copying of some files between the two repositories
 * possible.
 */
interface DLTService {
    val legalName: CordaX500Name get() = ourIdentity.name

    val ourIdentity: Party
}
