package alarmmanager.com.alarmmanageroreosupportdemo.data;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import java.util.List;


public class OffersViewModel extends ViewModel {
    public List<Hero> offer;
    private OfferDAO offerDAO;

    public OffersViewModel(Context ctx){
        offerDAO = LocalRepository.getOfferDatabase(ctx).offerDAO();

        //start one time task using work manager
        //RefreshScheduler.refreshCouponOneTimeWork((LifecycleOwner)ctx);
    }

    public List<Hero> getLatestData(){
        if(offer == null){
            offer = offerDAO.getAllRecords();
        }
        return offer;
    }
    public static class OffersViewModelFactory extends
            ViewModelProvider.NewInstanceFactory {
        private Context context;
        public OffersViewModelFactory(Context ctx) {
            context = ctx;
        }
        @Override
        public <T extends ViewModel> T create(Class<T> viewModel) {
            return (T) new OffersViewModel(context);
        }
    }
}
