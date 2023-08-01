CIVL=java -jar -Xmx20g ../civl.jar
VERIFY1=verify -input_mpi_nprocs=1
VERIFY2=verify -input_mpi_nprocs=2
VERIFY3=verify -input_mpi_nprocs=3
VERIFY4=verify -input_mpi_nprocs=4
VERIFY5=verify -input_mpi_nprocs_hi=5 -input_mpi_nprocs_lo=1 

all: mpi_bcast mpi_gather mpi_allgather mpi_reduce mpi_reduce_scatter mpi_reduce_scatter_nc mpi_allreduce mpi_allreduce_dr mpi_scatter exchange diff1d dotProd oddEven matmat cyc update

bad: mpi_bcast_bad mpi_gather_bad mpi_allgather_bad mpi_reduce_bad mpi_reduce_scatter_bad mpi_reduce_scatter_nc_bad mpi_allreduce_bad mpi_allreduce_dr_bad mpi_scatter_bad exchange_bad diff1d_bad dotProd_bad oddEven_bad matmat_bad cyc_bad update_bad

config:
	$(CIVL) config


mpi_bcast: MPI_Bcast.c
	$(CIVL) $(VERIFY5) -mpiContract=bcast MPI_Bcast.c

mpi_bcast_bad: MPI_Bcast_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=bcast MPI_Bcast_bad.c

mpi_gather: MPI_Gather.c
	$(CIVL) $(VERIFY5) -mpiContract=gather MPI_Gather.c

mpi_gather_bad: MPI_Gather_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=gather MPI_Gather_bad.c

mpi_allgather: MPI_Allgather.c
	$(CIVL) $(VERIFY5) -mpiContract=allgather MPI_Allgather.c

mpi_allgather_bad: MPI_Allgather_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=allgather MPI_Allgather_bad.c

mpi_reduce: MPI_Reduce.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce MPI_Reduce.c

mpi_reduce_bad: MPI_Reduce_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce MPI_Reduce_bad.c

mpi_reduce_scatter: MPI_Reduce_scatter.c 
	$(CIVL) $(VERIFY5) -mpiContract=reduce_scatter MPI_Reduce_scatter.c

mpi_reduce_scatter_bad: MPI_Reduce_scatter_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce_scatter MPI_Reduce_scatter_bad.c

mpi_reduce_scatter_nc: MPI_Reduce_scatter_nc.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce_scatter MPI_Reduce_scatter_nc.c

mpi_reduce_scatter_nc_bad: MPI_Reduce_scatter_nc_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce_scatter MPI_Reduce_scatter_nc_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=reduce_scatter MPI_Reduce_scatter_nc_init_offsets_bad.c

mpi_allreduce: MPI_Allreduce.c
	$(CIVL) $(VERIFY5) -mpiContract=allreduce MPI_Allreduce.c

mpi_allreduce_bad: MPI_Allreduce_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=allreduce MPI_Allreduce_bad.c

mpi_allreduce_dr: MPI_Allreduce_double_recursive.c
	$(CIVL) $(VERIFY5) -mpiContract=allreduce MPI_Allreduce_double_recursive.c

mpi_allreduce_dr_bad: MPI_Allreduce_double_recursive_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=allreduce MPI_Allreduce_double_recursive_bad.c

mpi_scatter: MPI_Scatter.c
	$(CIVL) $(VERIFY5) -mpiContract=scatter MPI_Scatter.c

mpi_scatter_bad: MPI_Scatter_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=scatter MPI_Scatter_bad.c

exchange: diffusion1d.c
	$(CIVL) $(VERIFY5) -mpiContract=exchange_ghost_cells diffusion1d.c

exchange_bad: exchange_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=exchange_ghost_cells exchange_bad.c

update: diffusion1d.c
	$(CIVL) $(VERIFY1) -mpiContract=update -loop diffusion1d.c

update_bad: update_bad.c
	$(CIVL) $(VERIFY1) -mpiContract=update -loop update_bad.c

diff1d: diffusion1d.c
	$(CIVL) $(VERIFY5) -mpiContract=diff1d_iter diffusion1d.c

diff1d_bad: diffusion1d_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=diff1d_iter diffusion1d_bad.c

dotProd: dotProd.c
	$(CIVL) $(VERIFY5) -mpiContract=hypre_ParVectorInnerProd dotProd.c

dotProd_bad: dotProd_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=hypre_ParVectorInnerProd dotProd_bad.c

oddEven: oddEvenSort.c
	$(CIVL) $(VERIFY5) -mpiContract=oddEvenParIter oddEvenSort.c

oddEven_bad: oddEvenSort_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=oddEvenParIter oddEvenSort_bad.c

matmat: matmat.c
	$(CIVL) $(VERIFY5) -mpiContract=matmat matmat.c

matmat_bad: matmat_bad.c
	$(CIVL) $(VERIFY5) -mpiContract=matmat matmat_bad.c

cyc: cyc.c
	$(CIVL) $(VERIFY5) -mpiContract=g cyc.c
	$(CIVL) $(VERIFY5) -mpiContract=f cyc.c


clean:
	rm -rf *~ CIVLREP
