package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, CSV, построчный текст); главное,
 * чтобы {@code load(save(quotes))} возвращал те же котировки. Проверьте себя тестом
 * {@code QuoteFileStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class QuoteFileStoreSkeleton implements QuoteFileStore {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void save(Path path, List<Quote> quotes) {
        // TODO: сохранить котировки в файл по указанному пути.
        //  Подсказка: на каждую котировку нужно сохранить тикер, тип и валюту инструмента (чтобы
        //  потом восстановить его через InstrumentFactories.create), а также цену и изменение за
        //  период — этого достаточно, чтобы собрать Quote обратно.
        List<QuoteRecord> records = quotes.stream()
                .map(QuoteRecord::from)
                .toList();
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), records);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в файл " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        // TODO: прочитать котировки из файла и восстановить при старте.
        //  Если файла ещё нет (первый запуск) — вернуть пустой список, а не бросать исключение.
        if (Files.notExists(path)) {
            return List.of();
        }
        try {
            List<QuoteRecord> records = mapper.readValue(path.toFile(), new TypeReference<>() {
            });
            return records.stream()
                    .map(QuoteRecord::toQuote)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из файла " + path, e);
        }
    }

    /** Запись об одной котировке в JSON-файле: из этих полей при чтении собирается {@link Quote} */
    private record QuoteRecord(String ticker, String type, String name, Currency currency,
                               BigDecimal price, BigDecimal changePercent) {

        static QuoteRecord from(Quote quote) {
            Instrument instrument = quote.getInstrument();
            return new QuoteRecord(instrument.getTicker(), instrument.getType(), instrument.getName(),
                    instrument.getCurrency(), quote.getPrice(), quote.getChangePercent());
        }

        Quote toQuote() {
            Instrument instrument = InstrumentFactories.create(type, ticker, name, currency);
            return new Quote(instrument, price, changePercent);
        }
    }
}
