package academy.backend.market_pulse.history;

import academy.backend.market_pulse.history.impl.PriceHistoryStoreSkeleton;

/** Самопроверка {@link PriceHistoryStoreSkeleton} контрактом {@link PriceHistoryStoreContractTest} */
class PriceHistoryStoreSkeletonTest extends PriceHistoryStoreContractTest {

    @Override
    protected PriceHistoryStore createStore() {
        return new PriceHistoryStoreSkeleton();
    }
}
