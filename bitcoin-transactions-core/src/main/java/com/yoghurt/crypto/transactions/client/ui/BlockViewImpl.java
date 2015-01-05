package com.yoghurt.crypto.transactions.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yoghurt.crypto.transactions.client.di.BitcoinPlaceRouter;
import com.yoghurt.crypto.transactions.client.util.BlockPartColorPicker;
import com.yoghurt.crypto.transactions.client.util.FormatUtil;
import com.yoghurt.crypto.transactions.client.widget.BlockHexViewer;
import com.yoghurt.crypto.transactions.client.widget.BlockViewer;
import com.yoghurt.crypto.transactions.client.widget.HashHexViewer;
import com.yoghurt.crypto.transactions.client.widget.TransactionHexViewer;
import com.yoghurt.crypto.transactions.client.widget.ValueViewer;
import com.yoghurt.crypto.transactions.shared.domain.Block;
import com.yoghurt.crypto.transactions.shared.domain.BlockInformation;
import com.yoghurt.crypto.transactions.shared.domain.BlockPartType;
import com.yoghurt.crypto.transactions.shared.domain.RawBlockContainer;
import com.yoghurt.crypto.transactions.shared.domain.RawTransactionContainer;
import com.yoghurt.crypto.transactions.shared.domain.Transaction;
import com.yoghurt.crypto.transactions.shared.util.block.BlockEncodeUtil;
import com.yoghurt.crypto.transactions.shared.util.transaction.TransactionEncodeUtil;

@Singleton
public class BlockViewImpl extends Composite implements BlockView {
  interface BlockViewImplUiBinder extends UiBinder<Widget, BlockViewImpl> {}

  private static final BlockViewImplUiBinder UI_BINDER = GWT.create(BlockViewImplUiBinder.class);

  @UiField FlowPanel extraInformationContainer;
  @UiField Label notFoundLabel;

  @UiField(provided = true) HashHexViewer blockHashViewer;

  @UiField(provided = true) ValueViewer versionViewer;
  @UiField(provided = true) BlockViewer previousBlockHashViewer;
  @UiField(provided = true) ValueViewer merkleRootViewer;
  @UiField(provided = true) ValueViewer timestampViewer;
  @UiField(provided = true) ValueViewer bitsViewer;
  @UiField(provided = true) ValueViewer nonceViewer;

  @UiField(provided = true) ValueViewer heightViewer;
  @UiField(provided = true) ValueViewer numConfirmationsViewer;
  @UiField(provided = true) ValueViewer numTransactionsViewer;
  @UiField(provided = true) BlockViewer nextBlockViewer;
  @UiField(provided = true) ValueViewer sizeViewer;

  @UiField BlockHexViewer blockHexViewer;
  @UiField TransactionHexViewer coinbaseHexViewer;

  @Inject
  public BlockViewImpl(final BitcoinPlaceRouter router) {
    blockHashViewer = new HashHexViewer();
    versionViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.VERSION));
    previousBlockHashViewer = new BlockViewer(router);
    merkleRootViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.MERKLE_ROOT));
    timestampViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.TIMESTAMP));
    bitsViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.BITS));
    nonceViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.NONCE));

    heightViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.PREV_BLOCK_HASH));
    numConfirmationsViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.VERSION));
    numTransactionsViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.MERKLE_ROOT));
    nextBlockViewer = new BlockViewer(router);
    sizeViewer = new ValueViewer(BlockPartColorPicker.getFieldColor(BlockPartType.TIMESTAMP));

    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setBlock(final Block block) {
    if(block == null) {
      return;
    }

    blockHashViewer.setHash(block.getBlockHash());

    versionViewer.setValue(block.getVersion());
    previousBlockHashViewer.setValue(block.getPreviousBlockHash());
    merkleRootViewer.setValue(block.getMerkleRoot());
    timestampViewer.setValue(FormatUtil.formatDateTime(block.getTimestamp()));
    bitsViewer.setValue(block.getBits());
    nonceViewer.setValue(block.getNonce());

    final RawBlockContainer rawBlock = new RawBlockContainer();
    try {
      BlockEncodeUtil.encodeBlock(block, rawBlock);
    } catch (final Throwable e) {
      e.printStackTrace();
      // Eat.
    }

    blockHexViewer.setContainer(rawBlock);
  }

  @Override
  public void setBlockInformation(final BlockInformation blockInformation, final Transaction coinbase) {
    notFoundLabel.setVisible(blockInformation == null);
    extraInformationContainer.setVisible(blockInformation != null);

    if (blockInformation == null) {
      return;
    }



    final RawTransactionContainer rawTransaction = new RawTransactionContainer();
    try {
      TransactionEncodeUtil.encodeTransaction(coinbase, rawTransaction);
    } catch (final Throwable e) {
      e.printStackTrace();
      // Eat.
    }

    coinbaseHexViewer.resetContainer(rawTransaction);

    // Do this deferredly because resetting the transaction may take up some CPU time.
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        heightViewer.setValue(blockInformation.getHeight());
        numConfirmationsViewer.setValue(blockInformation.getNumConfirmations());
        numTransactionsViewer.setValue(blockInformation.getNumTransactions());
        nextBlockViewer.setValue(blockInformation.getNextBlockHash());
        sizeViewer.setValue(blockInformation.getSize());
      }
    });
  }
}
