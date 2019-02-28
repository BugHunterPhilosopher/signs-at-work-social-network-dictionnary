package com.orange.signsatwork.biz.persistence.service.impl;

/*
 * #%L
 * Signs at work
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.orange.signsatwork.biz.domain.DailymotionCode;
import com.orange.signsatwork.biz.persistence.model.DailymotionCodeDB;
import com.orange.signsatwork.biz.persistence.service.DailymotionCodeService;
import com.orange.signsatwork.biz.persistence.repository.DailymotionCodeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailymotionCodeServiceImpl implements DailymotionCodeService {
  private final DailymotionCodeRepository dailymotionCodeRepository;

  @Override
  public DailymotionCode findLast() {
    return dailymotionCodeFrom(dailymotionCodeRepository.findLast());
  }

  @Override
  public DailymotionCode create(DailymotionCode code) {
    DailymotionCodeDB dailymotionCodeDB = dailymotionCodeRepository.save(dailymotionCodeDBFrom(code));
    return dailymotionCodeFrom(dailymotionCodeDB);
  }


  private DailymotionCode dailymotionCodeFrom(DailymotionCodeDB dailymotionCodeDB) {
    return new DailymotionCode(dailymotionCodeDB.getId(), dailymotionCodeDB.getCode());
  }

  private DailymotionCodeDB dailymotionCodeDBFrom(DailymotionCode dailymotionCode) {
    return new DailymotionCodeDB(dailymotionCode.getCode());
  }

}
