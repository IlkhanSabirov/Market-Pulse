package academy.backend.market_pulse.repository.store;

import academy.backend.market_pulse.repository.store.impl.QuoteFileStoreSkeleton;

/** Самопроверка {@link QuoteFileStoreSkeleton} контрактом {@link QuoteFileStoreContractTest} */
class QuoteFileStoreSkeletonTest extends QuoteFileStoreContractTest {

    @Override
    protected QuoteFileStore createStore() {
        return new QuoteFileStoreSkeleton();
    }
}
