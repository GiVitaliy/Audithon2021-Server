package ru.audithon.egissostat.logic.address.bl;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.audithon.egissostat.domain.address.Address;
import ru.audithon.common.bl.ObjectLogicBase;
import ru.audithon.common.processing.BeanProcessingBase;
import ru.audithon.common.processing.BeanProcessor;
import ru.audithon.common.validation.BeanValidationBase;

import static ru.audithon.common.helpers.StringUtils.processDataTextField;
import static ru.audithon.common.validation.BeanCondition.when;
import static ru.audithon.common.validation.BeanValidationBuilder.forField;
import static ru.audithon.common.validation.FieldConstraints.notEmpty;
import static ru.audithon.common.validation.FieldConstraints.notNull;

@Service
public class AddressBl extends ObjectLogicBase<Address> {

    @Autowired
    public AddressBl(MessageSource messageSource) {
        super(
                when(address -> Strings.isNullOrEmpty(address.getOther()),
                        BeanValidationBase.<Address>builder(messageSource)
                                .add(forField("regionId", "address", Address::getRegionId)
                                        .add(notNull()).build())
                                .add(forField("cityId", "address", Address::getCityId)
                                        .add(notNull()).build())
                                .add(forField("streetId", "address", Address::getStreetId)
                                        .add(notNull()).build())
                                .add(forField("house", "address", Address::getHouse)
                                        .add(notEmpty()).build())
                                .build()),

                BeanProcessingBase.<Address>builder()
                        .add(new BeanProcessor<>(
                                ad -> {
                                    if (ad == null) {
                                        return;
                                    }

                                    ad.setHouse(processDataTextField(ad.getHouse()));
                                    ad.setBuilding(processDataTextField(ad.getBuilding()));
                                    ad.setRoom(processDataTextField(ad.getRoom()));
                                    ad.setOther(processDataTextField(ad.getOther()));
                                }))
                        .build());
    }
}
