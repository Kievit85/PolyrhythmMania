package polyrhythmmania.storymode.inbox

import paintbox.binding.ReadOnlyVar
import paintbox.binding.asReadOnlyVar
import polyrhythmmania.storymode.StoryL10N
import polyrhythmmania.storymode.contract.Contract
import polyrhythmmania.storymode.contract.IHasContractTextInfo
import polyrhythmmania.storymode.contract.Requester


sealed class InboxItem(
        val id: String,

        val listingName: ReadOnlyVar<String>,
) {

    class Debug(id: String, listingName: String, val subtype: DebugSubtype, val description: String = "<no desc>")
        : InboxItem(id, ReadOnlyVar.const(listingName)) {

        enum class DebugSubtype {
            PROGRESSION_ADVANCER,
        }
    }

    class Memo(
            id: String, val hasToField: Boolean, hasSeparateListingName: Boolean,
            val subject: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.memo.$id.subject"),
            val from: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.memo.$id.from"),
            val to: ReadOnlyVar<String> = if (hasToField) StoryL10N.getVar("inboxItemDetails.memo.$id.to") else "".asReadOnlyVar(),
            val desc: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.memo.$id.desc"),
    ) : InboxItem(id, if (hasSeparateListingName) StoryL10N.getVar("inboxItemDetails.memo.$id.listing") else subject)
    
    class InfoMaterial(
            id: String, hasSeparateListingName: Boolean,
            val topic: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.infoMaterial.$id.topic"),
            val audience: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.infoMaterial.$id.audience"),
            val desc: ReadOnlyVar<String> = StoryL10N.getVar("inboxItemDetails.infoMaterial.$id.desc"),
    ) : InboxItem(id, if (hasSeparateListingName) StoryL10N.getVar("inboxItemDetails.infoMaterial.$id.listing") else topic)

    class ContractDoc(
            val contract: Contract, itemID: String = "contract_${contract.id}",
            listingName: ReadOnlyVar<String> = contract.name,
            val subtype: ContractSubtype = ContractSubtype.NORMAL,
    ) : InboxItem(itemID, listingName), IHasContractTextInfo by contract {

        enum class ContractSubtype(val headingL10NKey: String) {
            NORMAL("inboxItem.contract.heading.normal"),
            TRAINING("inboxItem.contract.heading.training"),
        }

        val headingText: ReadOnlyVar<String> = StoryL10N.getVar(subtype.headingL10NKey)
    }
    
    class PlaceholderContract(
            itemID: String, placeholderNumber: Int,
            override val name: ReadOnlyVar<String> = "PLACEHOLDER-${placeholderNumber.toString().padStart(3, '0')}".asReadOnlyVar(),
            override val desc: ReadOnlyVar<String> = "Placeholder contract desc".asReadOnlyVar(),
            override val tagline: ReadOnlyVar<String> = "Make up a tagline later!".asReadOnlyVar(),
            override val requester: Requester = Requester.DEBUG,
            listingName: ReadOnlyVar<String> = name,
            val subtype: ContractDoc.ContractSubtype = ContractDoc.ContractSubtype.NORMAL,
    ) : InboxItem(itemID, listingName), IHasContractTextInfo {

        val headingText: ReadOnlyVar<String> = StoryL10N.getVar(subtype.headingL10NKey)
    }

}
