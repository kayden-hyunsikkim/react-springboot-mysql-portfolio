import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* 2 errors occured about menuitem and translate component so I commented it  */}
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/portfolio">
        <Translate contentKey="global.menu.entities.portfolio" />
      </MenuItem>

      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
